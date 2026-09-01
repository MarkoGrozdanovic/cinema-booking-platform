import { Link } from "react-router";

function NotFoundPage() {
  return (
    <section>
      <h1 className="text-3xl font-bold">Page not found</h1>

      <Link
        className="mt-4 inline-block text-red-400 hover:text-red-300"
        to="/"
      >
        Return home
      </Link>
    </section>
  );
}

export default NotFoundPage;
