import { Route, Routes } from "react-router";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import AppLayout from "./components/layout/AppLayout";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import NotFoundPage from "./pages/NotFoundPage";
import RegisterPage from "./pages/RegisterPage";
import BookingsPage from "./pages/BookingsPage";
import ScreeningDetailsPage from "./pages/ScreeningDetailsPage";
import PaymentPage from "./pages/PaymentPage";
import PaymentSuccessPage from "./pages/PaymentSuccessPage";
import AdminRoute from "./components/auth/AdminRoute";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import CreateScreeningPage from "./pages/admin/CreateScreeningPage";
import AdminMoviesPage from "./pages/admin/AdminMoviesPage";
import CreateMoviePage from "./pages/admin/CreateMoviePage";
import AdminCinemasPage from "./pages/admin/AdminCinemaPage";
import CreateCinemaPage from "./pages/admin/CreateCinemaPage";
import AdminHallsPage from "./pages/admin/AdminHallsPage";
import CreateHallPage from "./pages/admin/CreateHallPage";

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<HomePage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        <Route element={<AdminRoute />}>
          <Route path="admin" element={<AdminDashboardPage />} />

          <Route
            path="admin/screenings/new"
            element={<CreateScreeningPage />}
          />

          <Route path="admin/movies" element={<AdminMoviesPage />} />

          <Route path="admin/movies/new" element={<CreateMoviePage />} />

          <Route path="admin/cinemas" element={<AdminCinemasPage />} />

          <Route path="admin/cinemas/new" element={<CreateCinemaPage />} />

          <Route path="admin/halls" element={<AdminHallsPage />} />

          <Route path="admin/halls/new" element={<CreateHallPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route path="bookings" element={<BookingsPage />} />

          <Route
            path="screenings/:screeningId"
            element={<ScreeningDetailsPage />}
          />

          <Route path="bookings/:bookingId/payment" element={<PaymentPage />} />

          <Route path="payment/success" element={<PaymentSuccessPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

export default App;
