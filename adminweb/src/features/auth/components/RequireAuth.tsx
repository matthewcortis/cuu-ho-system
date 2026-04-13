import { Navigate, Outlet, useLocation } from "react-router";
import { isAuthenticated } from "@/features/auth/utils/authSession";

type SignInLocationState = {
  from?: string;
};

export default function RequireAuth() {
  const location = useLocation();
  const state = location.state as SignInLocationState | null;
  const fromPath = state?.from;

  if (!isAuthenticated()) {
    return (
      <Navigate
        to="/signin"
        replace
        state={{ from: fromPath && fromPath.startsWith("/") ? fromPath : location.pathname }}
      />
    );
  }

  return <Outlet />;
}

