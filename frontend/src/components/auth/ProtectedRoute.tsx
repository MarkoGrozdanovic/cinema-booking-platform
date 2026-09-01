import { Navigate, Outlet, useLocation } from "react-router";
import { useAppSelector } from "../../store/hooks";

function ProtectedRoute() {
  const location = useLocation();

  const user = useAppSelector((state) => state.auth.user);

  if (!user) {
    return <Navigate replace to="/login" state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

export default ProtectedRoute;
