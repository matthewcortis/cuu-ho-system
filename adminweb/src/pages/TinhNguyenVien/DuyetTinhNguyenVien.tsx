import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import DuyetTinhNguyenVien from "../../components/tables/TinhNguyenVien/DuyetTinhNguyenVien";

export default function DuyetTinhNguyenVienPage() {
  return (
    <>
      <PageMeta
        title="Duyệt Tình Nguyện Viên"
        description="Trang quản lý tình nguyện viên, hiển thị danh sách các tình nguyện viên và thông tin liên quan."
      />
      <PageBreadcrumb pageTitle="Duyệt Tình Nguyện Viên" />
      <div className="space-y-6">
        <ComponentCard title="Danh sách tình nguyện viên">
          <DuyetTinhNguyenVien />
        </ComponentCard>
      </div>
    </>
  );
}
