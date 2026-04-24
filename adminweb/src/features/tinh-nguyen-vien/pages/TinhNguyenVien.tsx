import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import DanhSachTinhNguyenVien from "@/features/tinh-nguyen-vien/components/DanhSachTinhNguyenVien";

export default function TinhNguyenVien() {
  return (
    <>
      <PageMeta
        title="Tình Nguyện Viên"
        description="Trang quản lý tình nguyện viên đã được duyệt và điều phối vào đội nhóm."
      />
      <PageBreadcrumb pageTitle="Tình Nguyện Viên" />
      <div className="space-y-6">
        <ComponentCard title="Danh sách tình nguyện viên đã duyệt">
          <DanhSachTinhNguyenVien />
        </ComponentCard>
      </div>
    </>
  );
}
