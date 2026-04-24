import { BrowserRouter as Router, Navigate, Route, Routes } from "react-router";
import { ScrollToTop } from "@/components/common/ScrollToTop";
import AppLayout from "@/components/layout/AppLayout";
import RequireAuth from "@/features/auth/components/RequireAuth";
import SignIn from "@/features/auth/pages/SignIn";
import SignUp from "@/features/auth/pages/SignUp";
import { isAuthenticated } from "@/features/auth/utils/authSession";
import NotFound from "@/pages/OtherPage/NotFound";
import UserProfiles from "@/pages/UserProfiles";
import Videos from "@/pages/UiElements/Videos";
import Images from "@/pages/UiElements/Images";
import Alerts from "@/pages/UiElements/Alerts";
import Badges from "@/pages/UiElements/Badges";
import Avatars from "@/pages/UiElements/Avatars";
import Buttons from "@/pages/UiElements/Buttons";
import LineChart from "@/pages/Charts/LineChart";
import BarChart from "@/pages/Charts/BarChart";
import Calendar from "@/pages/Calendar";
import BasicTables from "@/pages/Tables/BasicTables";
import FormElements from "@/pages/Forms/FormElements";
import Blank from "@/pages/Blank";
import Home from "@/pages/Dashboard/Home";
import ChatPage from "@/features/chat/pages/ChatPage";
import TinhNguyenVien from "@/features/tinh-nguyen-vien/pages/TinhNguyenVien";
import DuyetTinhNguyenVienPage from "@/features/tinh-nguyen-vien/pages/DuyetTinhNguyenVien";
import DoiNhom from "@/features/tinh-nguyen-vien/pages/DoiNhom";
import NguoiDungPage from "@/features/nguoi-dung/pages/NguoiDungPage";
import BanDoPage from "@/features/phieu-cuu-tro/pages/BanDo";
import YeuCauPhieuCuuTroPage from "@/features/phieu-cuu-tro/pages/YeuCauPhieuCuuTro";
import LichSuCuuTroPage from "@/features/phieu-cuu-tro/pages/LichSuCuuTro";
import DonViPage from "@/features/vat-pham/pages/DonVi";
import NhomVatPhamPage from "@/features/vat-pham/pages/NhomVatPham";
import DanhSachVatPhamPage from "@/features/vat-pham/pages/DanhSachVatPham";
import ThemVatPhamPage from "@/features/vat-pham/pages/ThemVatPham";
import LoaiSuCoPage from "@/features/loai_su_co/pages/loai_su_co";

export default function AppRoutes() {
  return (
    <Router>
      <ScrollToTop />
      <Routes>
        <Route element={<RequireAuth />}>
          <Route element={<AppLayout />}>
            <Route index element={<Home />} />

            <Route path="/profile" element={<UserProfiles />} />
            <Route path="/calendar" element={<Calendar />} />
            <Route path="/blank" element={<Blank />} />
            <Route path="/chat" element={<ChatPage />} />

            <Route path="/ban-do" element={<BanDoPage />} />
            <Route path="/yeu-cau-phieu-cuu-tro" element={<YeuCauPhieuCuuTroPage />} />
            <Route path="/lich-su-cuu-tro" element={<LichSuCuuTroPage />} />
            <Route path="/nhom-vat-pham" element={<NhomVatPhamPage />} />
            <Route path="/don-vi-vat-pham" element={<DonViPage />} />
            <Route path="/loai-su-co" element={<LoaiSuCoPage />} />
            <Route path="/danh-sach-vat-pham" element={<DanhSachVatPhamPage />} />
            <Route path="/them-vat-pham" element={<ThemVatPhamPage />} />

            <Route path="/form-elements" element={<FormElements />} />
            <Route path="/nguoi-dung" element={<NguoiDungPage />} />

            <Route path="/basic-tables" element={<BasicTables />} />
            <Route path="/tinh-nguyen-vien" element={<TinhNguyenVien />} />
            <Route
              path="/duyet-tinh-nguyen-vien"
              element={<DuyetTinhNguyenVienPage />}
            />
            <Route path="/doi-nhom" element={<DoiNhom />} />

            <Route path="/alerts" element={<Alerts />} />
            <Route path="/avatars" element={<Avatars />} />
            <Route path="/badge" element={<Badges />} />
            <Route path="/buttons" element={<Buttons />} />
            <Route path="/images" element={<Images />} />
            <Route path="/videos" element={<Videos />} />

            <Route path="/line-chart" element={<LineChart />} />
            <Route path="/bar-chart" element={<BarChart />} />
          </Route>
        </Route>

        <Route
          path="/signin"
          element={isAuthenticated() ? <Navigate to="/" replace /> : <SignIn />}
        />
        <Route path="/signup" element={<SignUp />} />

        <Route path="*" element={<NotFound />} />
      </Routes>
    </Router>
  );
}
