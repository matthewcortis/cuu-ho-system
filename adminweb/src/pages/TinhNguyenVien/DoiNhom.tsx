import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import DanhSachDoiNhom from "../../components/tables/TinhNguyenVien/DanhSachDoiNhom";

export default function DoiNhom() {
  return (
    <>
      <PageMeta
        title="Đội Nhóm - Tình Nguyện Viên"
        description="Trang quản lý đội nhóm tình nguyện viên, hiển thị danh sách các đội nhóm và thông tin liên quan."
      />
      <PageBreadcrumb pageTitle="Đội Nhóm" />
      <div className="space-y-6">
        <ComponentCard title="Danh sách đội nhóm">
          <DanhSachDoiNhom />
        </ComponentCard>
      </div>
    </>
  );
}
