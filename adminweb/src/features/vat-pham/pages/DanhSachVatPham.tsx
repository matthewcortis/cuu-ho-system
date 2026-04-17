import { useEffect, useMemo, useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import Pagination from "@/components/common/Pagination";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import DanhSachVatPhamTable, {
  type DanhSachVatPhamItem,
  type VatPhamTrangThai,
} from "@/features/vat-pham/components/DanhSachVatPham";
import {
  fetchVatPhamList,
  type VatPhamDto,
  VatPhamApiError,
} from "@/features/vat-pham/api/vatPhamApi";

type TrangThaiFilter = "all" | VatPhamTrangThai;

const statusOptions: Array<{ value: TrangThaiFilter; label: string }> = [
  { value: "all", label: "All" },
  { value: "san_sang", label: "San sang" },
  { value: "ngung_cung_cap", label: "Ngung cung cap" },
];
const ROWS_PER_PAGE_OPTIONS = [10, 20, 50] as const;

function mapVatPhamDtoToItem(vatPham: VatPhamDto): DanhSachVatPhamItem {
  const soLuong = Number.isFinite(vatPham.soLuong) ? vatPham.soLuong : 0;
  const trangThai: VatPhamTrangThai = vatPham.trangThai ? "san_sang" : "ngung_cung_cap";
  const imageUrl = vatPham.tepTin?.duongDan?.trim() || null;

  return {
    id: vatPham.id,
    tenVatPham: vatPham.tenVatPham.trim() || "Chua dat ten",
    nhomVatPhamId: vatPham.nhomVatPham?.id ?? null,
    nhomVatPham: vatPham.nhomVatPham?.ten?.trim() || "Khong co nhom",
    donVi: vatPham.donVi?.ten?.trim() || "Khong co don vi",
    soLuong,
    trangThai,
    imageUrl,
    thieuAnh: !imageUrl,
    createdAt: vatPham.createdAt,
  };
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof VatPhamApiError) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the tai danh sach vat pham.";
}

export default function DanhSachVatPhamPage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const [selectedNhomId, setSelectedNhomId] = useState("all");
  const [statusFilter, setStatusFilter] = useState<TrangThaiFilter>("all");
  const [allItems, setAllItems] = useState<DanhSachVatPhamItem[]>([]);
  const [rowsPerPage, setRowsPerPage] = useState<number>(ROWS_PER_PAGE_OPTIONS[0]);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadVatPham = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const remoteItems = await fetchVatPhamList();
        if (!isMounted) {
          return;
        }
        setAllItems(remoteItems.map(mapVatPhamDtoToItem));
      } catch (error) {
        if (!isMounted) {
          return;
        }
        setLoadError(getApiErrorMessage(error));
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void loadVatPham();

    return () => {
      isMounted = false;
    };
  }, []);

  const nhomVatPhamOptions = useMemo(() => {
    const optionMap = new Map<string, string>();

    allItems.forEach((item) => {
      if (item.nhomVatPhamId === null) {
        return;
      }

      const key = String(item.nhomVatPhamId);
      if (!optionMap.has(key)) {
        optionMap.set(key, item.nhomVatPham || "Khong co ten");
      }
    });

    return Array.from(optionMap.entries()).map(([value, label]) => ({
      value,
      label,
    }));
  }, [allItems]);

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

  const totalFilteredItems = filteredItems.length;
  const totalPages = Math.max(1, Math.ceil(totalFilteredItems / rowsPerPage));

  const paginatedItems = useMemo(() => {
    const startIndex = (currentPage - 1) * rowsPerPage;
    return filteredItems.slice(startIndex, startIndex + rowsPerPage);
  }, [currentPage, filteredItems, rowsPerPage]);

  useEffect(() => {
    setCurrentPage(1);
  }, [rowsPerPage, searchKeyword, selectedNhomId, statusFilter]);

  useEffect(() => {
    setCurrentPage((prev) => Math.min(prev, totalPages));
  }, [totalPages]);

  return (
    <>
      <PageMeta
        title="Danh sach vat pham"
        description="Danh sach san pham theo nhom vat pham."
      />
      <PageBreadcrumb pageTitle="Danh sach vat pham" />
      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Khong the lay danh sach vat pham tu backend: {loadError}
          </div>
        )}

        <ComponentCard
          title="Danh sach chi tiet vat pham"
          desc="Tim kiem va loc theo danh sach."
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Dang tai danh sach vat pham...
            </div>
          )}

          <div className="space-y-5">
            <div className="grid grid-cols-1 gap-4 lg:grid-cols-4">
              <div>
                <Label htmlFor="search-vat-pham">Tim kiem san pham</Label>
                <Input
                  id="search-vat-pham"
                  type="text"
                  placeholder="Nhap ten san pham can tim"
                  value={searchKeyword}
                  onChange={(event) => setSearchKeyword(event.target.value)}
                />
              </div>

              <div>
                <Label htmlFor="filter-nhom">Loc theo nhom vat pham</Label>
                <select
                  id="filter-nhom"
                  value={selectedNhomId}
                  onChange={(event) => setSelectedNhomId(event.target.value)}
                  className="h-11 w-full appearance-none rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 pr-11 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                >
                  <option
                    value="all"
                    className="text-gray-700 dark:bg-gray-900 dark:text-gray-400"
                  >
                    Tat ca nhom vat pham
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
                <Label>Loc theo trang thai</Label>
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

              <div>
                <Label htmlFor="danh-sach-vat-pham-rows-per-page">So dong/trang</Label>
                <select
                  id="danh-sach-vat-pham-rows-per-page"
                  value={rowsPerPage}
                  onChange={(event) => setRowsPerPage(Number(event.target.value))}
                  className="h-11 w-full appearance-none rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 pr-11 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                >
                  {ROWS_PER_PAGE_OPTIONS.map((option) => (
                    <option
                      key={option}
                      value={option}
                      className="text-gray-700 dark:bg-gray-900 dark:text-gray-400"
                    >
                      {option}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <p className="text-theme-xs text-gray-500 dark:text-gray-400">
              Hien thi {paginatedItems.length}/{totalFilteredItems} vat pham (tong:{" "}
              {allItems.length})
            </p>

            <DanhSachVatPhamTable items={paginatedItems} />
            <Pagination
              currentPage={currentPage}
              totalItems={totalFilteredItems}
              itemsPerPage={rowsPerPage}
              totalItemsOverall={allItems.length}
              onPageChange={setCurrentPage}
            />
          </div>
        </ComponentCard>
      </div>
    </>
  );
}
