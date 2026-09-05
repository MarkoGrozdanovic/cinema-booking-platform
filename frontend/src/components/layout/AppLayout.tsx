import { Link, NavLink, Outlet, useNavigate } from "react-router";
import { logout } from "../../features/auth/authSlice";
import { useAppDispatch, useAppSelector } from "../../store/hooks";
import { clearAuthSession } from "../../features/auth/authStorage";

const navigationLinkClass = ({ isActive }: { isActive: boolean }) =>
  isActive
    ? "font-medium text-red-400"
    : "text-slate-300 transition hover:text-white";

function AppLayout() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const user = useAppSelector((state) => state.auth.user);

  function handleLogout() {
    clearAuthSession();
    dispatch(logout());
    navigate("/");
  }

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <header className="border-b border-slate-800 bg-slate-950/90">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <Link className="text-xl font-bold tracking-tight" to="/">
            Cinema Booking
          </Link>

          <div className="flex items-center gap-5">
            {user ? (
              <>
                {user?.role === "ADMIN" ? (
                  <NavLink
                    to="/admin"
                    className={({ isActive }) =>
                      isActive
                        ? "font-semibold text-amber-400"
                        : "text-slate-300 transition hover:text-white"
                    }
                  >
                    Admin
                  </NavLink>
                ) : (
                  <NavLink className={navigationLinkClass} to="/bookings">
                    My bookings
                  </NavLink>
                )}

                <span className="text-sm text-slate-300">
                  {user.firstName} {user.lastName}
                </span>

                <button
                  className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium transition hover:border-red-500 hover:text-red-400"
                  type="button"
                  onClick={handleLogout}
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <NavLink className={navigationLinkClass} to="/login">
                  Login
                </NavLink>

                <NavLink className={navigationLinkClass} to="/register">
                  Register
                </NavLink>
              </>
            )}
          </div>
        </nav>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-12">
        <Outlet />
      </main>
    </div>
  );
}

export default AppLayout;
