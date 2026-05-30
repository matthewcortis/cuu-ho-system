import { FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import { ApiError, dangNhap } from "@/features/auth/api/authApi";
import { ChevronLeftIcon, EyeCloseIcon, EyeIcon } from "@/icons";
import { saveAuthSession } from "@/features/auth/utils/authSession";
import Label from "@/components/form/Label";
import Checkbox from "@/components/form/input/Checkbox";
import Input from "@/components/form/input/InputField";
import Button from "@/components/ui/button/Button";

type SignInLocationState = {
  from?: string;
};

function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Dang nhap that bai. Vui long thu lai.";
}

export default function SignInForm() {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as SignInLocationState | null;
  const returnPath =
    locationState?.from && locationState.from.startsWith("/")
      ? locationState.from
      : "/";

  const [tenDangNhap, setTenDangNhap] = useState("");
  const [matKhau, setMatKhau] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isRemembered, setIsRemembered] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const normalizedTenDangNhap = tenDangNhap.trim();
    const rawMatKhau = matKhau;

    if (!normalizedTenDangNhap || !rawMatKhau.trim()) {
      setErrorMessage("Vui long nhap ten dang nhap va mat khau.");
      return;
    }

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      const data = await dangNhap({
        tenDangNhap: normalizedTenDangNhap,
        matKhau: rawMatKhau,
      });

      if (data.vaiTro !== "ADMIN") {
        setErrorMessage("Tai khoan khong co quyen truy cap admin web.");
        return;
      }

      saveAuthSession(data, isRemembered);
      navigate(returnPath, { replace: true });
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col flex-1">
      <div className="w-full max-w-md pt-10 mx-auto">
        <Link
          to="/"
          className="inline-flex items-center text-sm text-gray-500 transition-colors hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
        >
          <ChevronLeftIcon className="size-5" />
          Quay lai
        </Link>
      </div>
      <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
        <div>
          <div className="mb-5 sm:mb-8">
            <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
              Đăng nhập
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
                Nhập thông tin đăng nhập của bạn để truy cập vào trang quản trị.
              </p>
          </div>
          <div>
            <form onSubmit={handleSubmit}>
              <div className="space-y-6">
                <div>
                  <Label>
                    Tên Đăng Nhập <span className="text-error-500">*</span>
                  </Label>
                  <Input
                    name="tenDangNhap"
                    placeholder="Nhập tên đăng nhập"
                    value={tenDangNhap}
                    onChange={(event) => setTenDangNhap(event.target.value)}
                  />
                </div>
                <div>
                  <Label>
                    Mật khẩu <span className="text-error-500">*</span>
                  </Label>
                  <div className="relative">
                    <Input
                      name="matKhau"
                      type={showPassword ? "text" : "password"}
                      placeholder="Nhap mat khau"
                      value={matKhau}
                      onChange={(event) => setMatKhau(event.target.value)}
                    />
                    <span
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute z-30 -translate-y-1/2 cursor-pointer right-4 top-1/2"
                    >
                      {showPassword ? (
                        <EyeIcon className="fill-gray-500 dark:fill-gray-400 size-5" />
                      ) : (
                        <EyeCloseIcon className="fill-gray-500 dark:fill-gray-400 size-5" />
                      )}
                    </span>
                  </div>
                </div>

                {errorMessage ? (
                  <p className="text-sm text-error-500">{errorMessage}</p>
                ) : null}

                {/* <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <Checkbox checked={isRemembered} onChange={setIsRemembered} />
                    <span className="block font-normal text-gray-700 text-theme-sm dark:text-gray-400">
                      Ghi nhớ đăng nhập
                    </span>
                  </div>
                  <Link
                    to="/reset-password"
                    className="text-sm text-brand-500 hover:text-brand-600 dark:text-brand-400"
                  >
                    Quên mật khẩu?
                  </Link>
                </div> */}
                <div>
                  <Button className="w-full" size="sm" disabled={isSubmitting}>
                    {isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
                  </Button>
                </div>
              </div>
            </form>

          
          </div>
        </div>
      </div>
    </div>
  );
}

