import { useMemo, useState } from "react";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import Label from "../../components/form/Label";
import Input from "../../components/form/input/InputField";
import DanhSachVatPhamTable, {
  type DanhSachVatPhamItem,
  type VatPhamTrangThai,
} from "../../components/tables/VatPham/DanhSachVatPham";
import { mockDatabase } from "../../data/schemaMockData";

type TrangThaiFilter = "all" | VatPhamTrangThai;

const statusOptions: Array<{ value: TrangThaiFilter; label: string }> = [
  { value: "all", label: "All" },
  { value: "san_sang", label: "Sẵn sàng" },
  { value: "ngung_cung_cap", label: "Ngừng cung cấp" },
];

function buildVatPhamItems(): DanhSachVatPhamItem[] {
  const imageTepTinMap = new Map<number, string>();
  mockDatabase.tep_tin.forEach((tepTin) => {
    if (!tepTin.duong_dan || !tepTin.loai_tep_tin?.startsWith("image/")) return;
    imageTepTinMap.set(tepTin.id, tepTin.duong_dan);
  });

  return mockDatabase.vat_pham.map((vatPham) => {
    const nhomVatPham = mockDatabase.nhom_vat_pham.find(
      (item) => item.id === vatPham.nhom_vat_pham_id
    );
    const donVi = mockDatabase.don_vi.find((item) => item.id === vatPham.don_vi_id);

    const soLuong = vatPham.so_luong ?? 0;
    const trangThai: VatPhamTrangThai = soLuong > 60 ? "san_sang" : "ngung_cung_cap";

    return {
      id: vatPham.id,
      tenVatPham: vatPham.ten_vat_pham?.trim() || "Chua dat ten",
      nhomVatPhamId: vatPham.nhom_vat_pham_id,
      nhomVatPham: nhomVatPham?.ten || "Khong co nhom",
      donVi: donVi?.ten || "Khong co don vi",
      soLuong,
      trangThai,
      imageUrl: imageTepTinMap.get(vatPham.id) ?? null,
      thieuAnh: !imageTepTinMap.has(vatPham.id),
      createdAt: vatPham.created_at,
    };
  });
}

export default function DanhSachVatPhamPage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const [selectedNhomId, setSelectedNhomId] = useState("all");
  const [statusFilter, setStatusFilter] = useState<TrangThaiFilter>("all");

  const allItems = useMemo(() => buildVatPhamItems(), []);
  const nhomVatPhamOptions = useMemo(
    () =>
      mockDatabase.nhom_vat_pham.map((item) => ({
        value: String(item.id),
        label: item.ten || "Khong co ten",
      })),
    []
  );

  const filteredItems = useMemo(() => {
    const normalizedKeyword = searchKeyword.trim().toLowerCase();

    return allItems.filter((item) => {
      const matchesKeyword =
        normalizedKeyword.length === 0 ||
        item.tenVatPham.toLowerCase().includes(normalizedKeyword);
      const matchesNhom =
        selectedNhomId === "all" || String(item.nhomVatPhamId) === selectedNhomId;
      const matchesStatus = statusFilter === "all" || item.trangThai === statusFilter;

      return matchesKeyword && matchesNhom && matchesStatus;
    });
  }, [allItems, searchKeyword, selectedNhomId, statusFilter]);

  return (
    <>
      <PageMeta
        title="Danh sách vật phẩm"
        description="Danh sách sản phẩm theo nhóm vật phẩm."
      />
      <PageBreadcrumb pageTitle="Danh sách vật phẩm" />
      <div className="space-y-6">
        <ComponentCard
          title="Danh sách chi tiết vật phẩm"
          desc="Tìm kiếm và lọc theo danh sách."
        >
          <div className="space-y-5">
            <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
              <div>
                <Label htmlFor="search-vat-pham">Tìm kiếm sản phẩm</Label>
                <Input
                  id="search-vat-pham"
                  type="text"
                  placeholder="nhập tên sản phầm cần tìm"
                  value={searchKeyword}
                  onChange={(event) => setSearchKeyword(event.target.value)}
                />
              </div>

              <div>
                <Label htmlFor="filter-nhom">Lọc theo nhóm vật phẩm</Label>
                <select
                  id="filter-nhom"
                  value={selectedNhomId}
                  onChange={(event) => setSelectedNhomId(event.target.value)}
                  className="h-11 w-full appearance-none rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 pr-11 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                >
                  <option value="all" className="text-gray-700 dark:bg-gray-900 dark:text-gray-400">
                    Tất cả nhóm vật phẩm
                  </option>
                  {nhomVatPhamOptions.map((option) => (
                    <option
                      key={option.value}
                      value={option.value}
                      className="text-gray-700 dark:bg-gray-900 dark:text-gray-400"
                    >
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <Label>Lọc theo trạng thái</Label>
                <div className="flex flex-wrap items-center gap-2 rounded-lg border border-gray-200 p-2 dark:border-gray-700">
                  {statusOptions.map((option) => {
                    const isActive = statusFilter === option.value;

                    return (
                      <button
                        key={option.value}
                        type="button"
                        onClick={() => setStatusFilter(option.value)}
                        className={`rounded-lg px-3 py-2 text-theme-xs font-medium transition ${
                          isActive
                            ? "bg-brand-500 text-white"
                            : "bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-white/[0.05] dark:text-gray-300 dark:hover:bg-white/[0.1]"
                        }`}
                      >
                        {option.label}
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>

            <p className="text-theme-xs text-gray-500 dark:text-gray-400">
              Hiển thị {filteredItems.length}/{allItems.length} vật phẩm
            </p>

            <DanhSachVatPhamTable items={filteredItems} />
          </div>
        </ComponentCard>
      </div>
    </>
  );
}
