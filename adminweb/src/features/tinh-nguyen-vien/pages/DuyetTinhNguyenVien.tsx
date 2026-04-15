import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import DuyetTinhNguyenVien from "@/features/tinh-nguyen-vien/components/DuyetTinhNguyenVien";

export default function DuyetTinhNguyenVienPage() {
  return (
    <>
      <PageMeta
        title="Duyệt tình nguyện viên"
        description="Trang hiển thị danh sách tình nguyện viên đang chờ xét duyệt."
      />
      <PageBreadcrumb pageTitle="Duyệt tình nguyện viên" />
      <div className="space-y-6">
        <ComponentCard title="Danh sách tình nguyện viên chờ duyệt">
          <DuyetTinhNguyenVien />
        </ComponentCard>
      </div>
    </>
  );
}