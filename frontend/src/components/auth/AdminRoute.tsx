import { Navigate, Outlet, useLocation } from "react-router";
import { useAppSelector } from "../../store/hooks";

function AdminRoute() {
  const location = useLocation();

  const { user, accessToken } = useAppSelector((state) => state.auth);

  if (!user || !accessToken) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (user.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default AdminRoute;
