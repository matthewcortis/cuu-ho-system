import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import DanhSachTinhNguyenVien from "../../components/tables/TinhNguyenVien/DanhSachTinhNguyenVien";

export default function TinhNguyenVien() {
  return (
    <>
      <PageMeta
        title="Tình Nguyện Viên"
        description="Trang quản lý tình nguyện viên, hiển thị danh sách các tình nguyện viên và thông tin liên quan."
      />
      <PageBreadcrumb pageTitle="Tình Nguyện Viên" />
      <div className="space-y-6">
        <ComponentCard title="Danh sách tình nguyện viên ">
          <DanhSachTinhNguyenVien />
        </ComponentCard>
      </div>
    </>
  );
}
