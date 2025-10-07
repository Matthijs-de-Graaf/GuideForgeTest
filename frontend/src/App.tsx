import { useEffect, useState } from "react";
import axios from "axios";

interface AuthorDetails
{
  author_name: string;
  author_birthdate: string;
  author_country: string;
}

interface PublisherDetails
{
  publisher_name: string;
  publisher_city: string;
  publisher_country: string;
}

interface BooksDetails
{
  id: number;
  title: string;
  isbn: string;
  publication_date: string;
  pages: number;
  language: string;
  author: AuthorDetails;
  publisher: PublisherDetails;
}

function App() {
  const [books, setBooks] = useState<BooksDetails[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    setLoading(true);
    axios
      .get(`${import.meta.env.VITE_API_URL}/api/books/all`)
      .then((response) => {
        setBooks(response.data);
        setError(null);
        setLoading(false);
      })
      .catch((error) => {
        console.error("API error:", error);
        setError("Kon books niet ophalen. Probeer het later opnieuw.");
        setLoading(false);
      });
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Alle boeken</h1>
      {loading && <p>Laden...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}
      {!loading && !error && books.length > 0 ? (
        <ul style={{ listStyleType: "none", padding: 0 }}>
          {books.map((book) => (
            <li
              key={book.id}
              style={{
                padding: "10px",
                borderBottom: "1px solid #ccc",
              }}
            >
              <strong>{book.title}</strong> ({book.author.author_name})
            </li>
          ))}
        </ul>
      ) : (
        !loading && !error && <p>Geen boeken gevonden.</p>
      )}

    </div>
  );
}

export default App;