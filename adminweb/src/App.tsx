import { BrowserRouter as Router, Routes, Route } from "react-router";
import SignIn from "./pages/AuthPages/SignIn";
import SignUp from "./pages/AuthPages/SignUp";
import NotFound from "./pages/OtherPage/NotFound";
import UserProfiles from "./pages/UserProfiles";
import Videos from "./pages/UiElements/Videos";
import Images from "./pages/UiElements/Images";
import Alerts from "./pages/UiElements/Alerts";
import Badges from "./pages/UiElements/Badges";
import Avatars from "./pages/UiElements/Avatars";
import Buttons from "./pages/UiElements/Buttons";
import LineChart from "./pages/Charts/LineChart";
import BarChart from "./pages/Charts/BarChart";
import Calendar from "./pages/Calendar";
import BasicTables from "./pages/Tables/BasicTables";
import FormElements from "./pages/Forms/FormElements";
import Blank from "./pages/Blank";
import AppLayout from "./layout/AppLayout";
import { ScrollToTop } from "./components/common/ScrollToTop";
import Home from "./pages/Dashboard/Home";
import TinhNguyenVien from "./pages/TinhNguyenVien/TinhNguyenVien";
import DuyetTinhNguyenVienPage from "./pages/TinhNguyenVien/DuyetTinhNguyenVien";
import DoiNhom from "./pages/TinhNguyenVien/DoiNhom";
import NguoiDungPage from "./pages/NguoiDung/NguoiDungPage";
import BanDoPage from "./pages/PhieuCuuTro/BanDo";
import YeuCauPhieuCuuTroPage from "./pages/PhieuCuuTro/YeuCauPhieuCuuTro";
import LichSuCuuTroPage from "./pages/PhieuCuuTro/LichSuCuuTro";
export default function App() {
  return (
    <>
      <Router>
        <ScrollToTop />
        <Routes>
          {/* Dashboard Layout */}
          <Route element={<AppLayout />}>
            <Route index path="/" element={<Home />} />

            {/* Others Page */}
            <Route path="/profile" element={<UserProfiles />} />
            <Route path="/calendar" element={<Calendar />} />
            <Route path="/blank" element={<Blank />} />
             {/* PHEU CUU TRO */}
            <Route path="/ban-do" element={<BanDoPage />}/>
            <Route
              path="/yeu-cau-phieu-cuu-tro"
              element={<YeuCauPhieuCuuTroPage />}
            />
            <Route
              path="/lich-su-cuu-tro"
              element={<LichSuCuuTroPage />}
            />

            {/* Forms */}
            <Route path="/form-elements" element={<FormElements />} />
            <Route path="/nguoi-dung" element={<NguoiDungPage />}/>

            {/* Tables */}
            <Route path="/basic-tables" element={<BasicTables />} />
            <Route path="/tinh-nguyen-vien" element={<TinhNguyenVien />} />
            <Route
              path="/duyet-tinh-nguyen-vien"
              element={<DuyetTinhNguyenVienPage />}
            />
            <Route path="/doi-nhom" element={<DoiNhom />} />

            

            {/* Ui Elements */}
            <Route path="/alerts" element={<Alerts />} />
            <Route path="/avatars" element={<Avatars />} />
            <Route path="/badge" element={<Badges />} />
            <Route path="/buttons" element={<Buttons />} />
            <Route path="/images" element={<Images />} />
            <Route path="/videos" element={<Videos />} />

            {/* Charts */}
            <Route path="/line-chart" element={<LineChart />} />
            <Route path="/bar-chart" element={<BarChart />} />
          </Route>

          {/* Auth Layout */}
          <Route path="/signin" element={<SignIn />} />
          <Route path="/signup" element={<SignUp />} />

          {/* Fallback Route */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Router>
    </>
  );
}
