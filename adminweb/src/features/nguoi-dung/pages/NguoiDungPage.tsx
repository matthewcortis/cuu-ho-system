import { useCallback, useEffect, useMemo, useState } from "react";
import ComponentCard from "@/components/common/ComponentCard";
import PageBreadCrumb from "@/components/common/PageBreadCrumb";
import PageMeta from "@/components/common/PageMeta";
import Pagination from "@/components/common/Pagination";
import Badge from "@/components/ui/badge/Badge";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { EyeIcon } from "@/icons";
import { Modal } from "@/components/ui/modal";
import { useModal } from "@/hooks/useModal";
import ActionToast, {
  type ActionToastData,
  type ActionToastType,
} from "@/components/common/ActionToast";
import {
  fetchNguoiDungList,
  fetchPhieuCuuTroList,
  NguoiDungApiError,
  updateNguoiDungTaiKhoanTrangThai,
  type NguoiDungDto,
  type PhieuCuuTroDto,
} from "@/features/nguoi-dung/api/nguoiDungApi";

type NguoiDungTableItem = {
  id: string;
  taiKhoanId: number | null;
  ten: string;
  soDienThoai: string;
  diaChi: string;
  trangThaiKichHoat: boolean;
  tenDangNhap: string;
  email: string;
  avatarUrl: string;
  createdAt: string;
  toaDo: string;
  tongSoPhieuHoTro: number;
};

type VatPhamHoTroItem = {
  tenVatPham: string;
  soLuong: string;
  ghiChu: string;
};

type HoTroPhieuHistory = {
  id: number;
  trangThaiPhieu: string;
  thoiGianHoTro: string;
  nguoiNhan: string;
  soDienThoai: string;
  diaChi: string;
  ghiChu: string;
  vatPhamHoTro: VatPhamHoTroItem[];
};

type TrangThaiNguoiDungFilter = "all" | "active" | "inactive";

const DEFAULT_AVATAR = "/images/user/user-01.jpg";
const ROWS_PER_PAGE_OPTIONS = [20, 50, 100] as const;
const TRANG_THAI_FILTER_OPTIONS: Array<{
  value: TrangThaiNguoiDungFilter;
  label: string;
}> = [
    { value: "all", label: "All" },
    { value: "active", label: "Active" },
    { value: "inactive", label: "Inactive" },
  ];

function trimOrFallback(value: string | null | undefined, fallback: string): string {
  if (typeof value !== "string") {
    return fallback;
  }

  const normalized = value.trim();
  return normalized.length > 0 ? normalized : fallback;
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof NguoiDungApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the xu ly du lieu nguoi dung.";
}

function buildNguoiDungTableData(
  nguoiDungApiList: NguoiDungDto[],
  phieuCuuTroList: PhieuCuuTroDto[]
): NguoiDungTableItem[] {
  const tongPhieuByNguoiDungId = phieuCuuTroList.reduce<Record<string, number>>(
    (acc, phieu) => {
      const nguoiGui = phieu.nguoiGui;

      if (!nguoiGui || nguoiGui.type !== "NGUOI_DUNG" || !nguoiGui.userId) {
        return acc;
      }

      acc[nguoiGui.userId] = (acc[nguoiGui.userId] ?? 0) + 1;
      return acc;
    },
    {}
  );

  return nguoiDungApiList
    .map((nguoiDung) => ({
      id: nguoiDung.id,
      taiKhoanId: nguoiDung.taiKhoan?.id ?? null,
      ten: trimOrFallback(nguoiDung.ten, "Chua cap nhat"),
      soDienThoai: trimOrFallback(nguoiDung.sdt, "Chua cap nhat"),
      diaChi: trimOrFallback(nguoiDung.viTri?.diaChi, "Chua cap nhat"),
      trangThaiKichHoat: Boolean(nguoiDung.taiKhoan?.trangThai),
      tenDangNhap: trimOrFallback(nguoiDung.taiKhoan?.tenDangNhap, "Chua cap nhat"),
      email: trimOrFallback(nguoiDung.taiKhoan?.email, "Chua cap nhat"),
      avatarUrl: trimOrFallback(nguoiDung.avatarUrl, DEFAULT_AVATAR),
      createdAt: nguoiDung.createdAt,
      toaDo:
        nguoiDung.viTri?.lat && nguoiDung.viTri?.longitude
          ? `${nguoiDung.viTri.lat}, ${nguoiDung.viTri.longitude}`
          : "Chua cap nhat",
      tongSoPhieuHoTro: tongPhieuByNguoiDungId[nguoiDung.id] ?? 0,
    }))
    .sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
    );
}

function formatDateTime(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

function formatPhieuTrangThai(value: string): string {
  const normalized = value.trim();
  if (!normalized) {
    return "Chua cap nhat";
  }

  return normalized
    .split("_")
    .map((part) => {
      const lower = part.toLowerCase();
      return `${lower.slice(0, 1).toUpperCase()}${lower.slice(1)}`;
    })
    .join(" ");
}

function buildHoTroHistoryByNguoiDungId(
  nguoiDungId: string,
  fallbackNguoiNhan: string,
  phieuCuuTroList: PhieuCuuTroDto[]
): HoTroPhieuHistory[] {
  return phieuCuuTroList
    .filter(
      (phieu) =>
        phieu.nguoiGui?.type === "NGUOI_DUNG" && phieu.nguoiGui.userId === nguoiDungId
    )
    .sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
    )
    .map((phieu) => {
      const vatPhamHoTro =
        phieu.chiTietCuuTro.length > 0
          ? phieu.chiTietCuuTro.map((item) => ({
            tenVatPham: trimOrFallback(item.tenVatPham, "Chua co ten vat pham"),
            soLuong:
              item.soLuong === null || item.soLuong === undefined
                ? "Chua cap nhat"
                : String(item.soLuong),
            ghiChu: trimOrFallback(item.ghiChu, "Khong co ghi chu"),
          }))
          : [
            {
              tenVatPham: "Chua co thong tin vat pham",
              soLuong: "Chua cap nhat",
              ghiChu: "Phieu chua co danh sach chi tiet cuu tro",
            },
          ];

      return {
        id: phieu.id,
        trangThaiPhieu: formatPhieuTrangThai(phieu.trangThai),
        thoiGianHoTro: phieu.createdAt,
        nguoiNhan: trimOrFallback(phieu.nguoiGui?.ten, fallbackNguoiNhan),
        soDienThoai: trimOrFallback(phieu.nguoiGui?.sdt, "Chua cap nhat"),
        diaChi: trimOrFallback(phieu.viTri?.diaChi, "Chua cap nhat"),
        ghiChu: trimOrFallback(phieu.ghiChu, "Khong co ghi chu"),
        vatPhamHoTro,
      };
    });
}

export default function NguoiDungPage() {
  const [nguoiDungApiList, setNguoiDungApiList] = useState<NguoiDungDto[]>([]);
  const [phieuCuuTroList, setPhieuCuuTroList] = useState<PhieuCuuTroDto[]>([]);
  const [isNguoiDungLoading, setIsNguoiDungLoading] = useState<boolean>(false);
  const [nguoiDungApiError, setNguoiDungApiError] = useState<string | null>(null);
  const [actionToast, setActionToast] = useState<ActionToastData | null>(null);

  const nguoiDungList = useMemo(
    () => buildNguoiDungTableData(nguoiDungApiList, phieuCuuTroList),
    [nguoiDungApiList, phieuCuuTroList]
  );
  const {
    isOpen: isUserDetailDialogOpen,
    openModal: openUserDetailDialog,
    closeModal: closeUserDetailDialog,
  } = useModal();

  const [selectedNguoiDungId, setSelectedNguoiDungId] = useState<string>("");
  const [trangThaiByNguoiDungId, setTrangThaiByNguoiDungId] = useState<
    Record<string, boolean>
  >({});
  const [isUpdatingTrangThaiByNguoiDungId, setIsUpdatingTrangThaiByNguoiDungId] =
    useState<Record<string, boolean>>({});
  const [isHoTroListOpen, setIsHoTroListOpen] = useState<boolean>(false);
  const [expandedPhieuIds, setExpandedPhieuIds] = useState<Record<number, boolean>>({});
  const [trangThaiFilter, setTrangThaiFilter] =
    useState<TrangThaiNguoiDungFilter>("all");
  const [diaChiFilter, setDiaChiFilter] = useState<string>("");
  const [rowsPerPage, setRowsPerPage] = useState<number>(ROWS_PER_PAGE_OPTIONS[0]);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const showActionToast = useCallback(
    (type: ActionToastType, title: string, message: string) => {
      setActionToast({
        id: Date.now(),
        type,
        title,
        message,
      });
    },
    []
  );

  useEffect(() => {
    let isMounted = true;

    const loadNguoiDungData = async () => {
      setIsNguoiDungLoading(true);
      setNguoiDungApiError(null);

      try {
        const [nguoiDungResult, phieuResult] = await Promise.all([
          fetchNguoiDungList(),
          fetchPhieuCuuTroList(),
        ]);

        if (!isMounted) {
          return;
        }

        setNguoiDungApiList(nguoiDungResult);
        setPhieuCuuTroList(phieuResult);
      } catch (error) {
        if (!isMounted) {
          return;
        }


        setNguoiDungApiError(getApiErrorMessage(error));
        setNguoiDungApiList([]);
        setPhieuCuuTroList([]);
      } finally {
        if (isMounted) {
          setIsNguoiDungLoading(false);
        }
      }
    };

    void loadNguoiDungData();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (nguoiDungList.length === 0) {
      setSelectedNguoiDungId("");
      return;
    }

    const hasSelectedNguoiDung = nguoiDungList.some(
      (nguoiDung) => nguoiDung.id === selectedNguoiDungId
    );

    if (!hasSelectedNguoiDung) {
      setSelectedNguoiDungId(nguoiDungList[0].id);
    }
  }, [nguoiDungList, selectedNguoiDungId]);

  useEffect(() => {
    setTrangThaiByNguoiDungId(
      Object.fromEntries(
        nguoiDungList.map((nguoiDung) => [nguoiDung.id, nguoiDung.trangThaiKichHoat])
      )
    );
  }, [nguoiDungList]);

  const selectedNguoiDung = useMemo(
    () => nguoiDungList.find((nguoiDung) => nguoiDung.id === selectedNguoiDungId),
    [nguoiDungList, selectedNguoiDungId]
  );
  const selectedNguoiDungHoTroList = useMemo(
    () =>
      selectedNguoiDung
        ? buildHoTroHistoryByNguoiDungId(
          selectedNguoiDung.id,
          selectedNguoiDung.ten,
          phieuCuuTroList
        )
        : ([] as HoTroPhieuHistory[]),
    [selectedNguoiDung, phieuCuuTroList]
  );

  const getTrangThai = (nguoiDungId: string, defaultValue: boolean) =>
    trangThaiByNguoiDungId[nguoiDungId] ?? defaultValue;

  const filteredNguoiDungList = useMemo(() => {
    const normalizedDiaChi = diaChiFilter.trim().toLowerCase();

    return nguoiDungList.filter((nguoiDung) => {
      const isActivated =
        trangThaiByNguoiDungId[nguoiDung.id] ?? nguoiDung.trangThaiKichHoat;
      const matchesTrangThai =
        trangThaiFilter === "all" ||
        (trangThaiFilter === "active" && isActivated) ||
        (trangThaiFilter === "inactive" && !isActivated);
      const matchesDiaChi =
        normalizedDiaChi.length === 0 ||
        nguoiDung.diaChi.toLowerCase().includes(normalizedDiaChi);

      return matchesTrangThai && matchesDiaChi;
    });
  }, [diaChiFilter, nguoiDungList, trangThaiByNguoiDungId, trangThaiFilter]);

  const totalFilteredNguoiDung = filteredNguoiDungList.length;
  const totalPages = Math.max(1, Math.ceil(totalFilteredNguoiDung / rowsPerPage));

  const paginatedNguoiDungList = useMemo(() => {
    const startIndex = (currentPage - 1) * rowsPerPage;
    return filteredNguoiDungList.slice(startIndex, startIndex + rowsPerPage);
  }, [currentPage, filteredNguoiDungList, rowsPerPage]);

  useEffect(() => {
    setCurrentPage(1);
  }, [diaChiFilter, rowsPerPage, trangThaiFilter]);

  useEffect(() => {
    setCurrentPage((prev) => Math.min(prev, totalPages));
  }, [totalPages]);

  const handleChangeActivation = async (
    nguoiDung: NguoiDungTableItem,
    nextTrangThai: boolean
  ) => {
    const currentTrangThai = getTrangThai(nguoiDung.id, nguoiDung.trangThaiKichHoat);
    if (currentTrangThai === nextTrangThai) {
      return;
    }

    if (isUpdatingTrangThaiByNguoiDungId[nguoiDung.id]) {
      return;
    }

    setIsUpdatingTrangThaiByNguoiDungId((prev) => ({
      ...prev,
      [nguoiDung.id]: true,
    }));
    setTrangThaiByNguoiDungId((prev) => ({
      ...prev,
      [nguoiDung.id]: nextTrangThai,
    }));

    try {
      const updatedNguoiDung = await updateNguoiDungTaiKhoanTrangThai(
        nguoiDung.id,
        nextTrangThai
      );
      const isActivated = Boolean(updatedNguoiDung.taiKhoan?.trangThai);
      const message = isActivated
        ? `Da kich hoat tai khoan ${nguoiDung.ten}.`
        : `Da tam khoa tai khoan ${nguoiDung.ten}.`;

      setNguoiDungApiList((prev) =>
        prev.map((item) => (item.id === updatedNguoiDung.id ? updatedNguoiDung : item))
      );
      setTrangThaiByNguoiDungId((prev) => ({
        ...prev,
        [updatedNguoiDung.id]: Boolean(updatedNguoiDung.taiKhoan?.trangThai),
      }));
      showActionToast(
        "success",
        "Cap nhat trang thai tai khoan",
        message,
      );
    } catch (error) {
      const errorMessage = getApiErrorMessage(error);
      setTrangThaiByNguoiDungId((prev) => ({
        ...prev,
        [nguoiDung.id]: currentTrangThai,
      }));
      showActionToast("error", "Khong the cap nhat", errorMessage);
    } finally {
      setIsUpdatingTrangThaiByNguoiDungId((prev) => ({
        ...prev,
        [nguoiDung.id]: false,
      }));
    }
  };

  const handleOpenUserDetailDialog = (nguoiDungId: string) => {
    setSelectedNguoiDungId(nguoiDungId);
    setIsHoTroListOpen(false);
    setExpandedPhieuIds({});
    openUserDetailDialog();
  };

  const handleCloseUserDetailDialog = () => {
    setIsHoTroListOpen(false);
    setExpandedPhieuIds({});
    closeUserDetailDialog();
  };

  const handleTogglePhieuDropdown = (phieuId: number) => {
    setExpandedPhieuIds((prev) => ({
      ...prev,
      [phieuId]: !prev[phieuId],
    }));
  };

  return (
    <>
      <PageMeta
        title="Người dùng"
        description="Trang quản lý người dùng: danh sách, trạng thái tài khoản đang hoạt động và lịch sử tạo phiếu cứu trợ."
      />
      <PageBreadCrumb pageTitle="Người dùng" />

      <div className="space-y-6">
        <ComponentCard
          title="Danh sách người dùng"
          desc="Nhấn vào từng dòng để xem thông tin chi tiết và lịch sử tạo phiếu cứu trợ."
        >
          {isNguoiDungLoading && (
            <div className="rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Đang tải dữ liệu người dùng và phiếu cứu trợ từ backend...
            </div>
          )}

          {nguoiDungApiError && (
            <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
              Không thể tải đầy đủ dữ liệu API: {nguoiDungApiError}
            </div>
          )}

          <div className="mb-5 grid grid-cols-1 gap-4 lg:grid-cols-3">
            <div>
              <Label htmlFor="nguoi-dung-trang-thai-filter">Lọc trạng thái</Label>
              <select
                id="nguoi-dung-trang-thai-filter"
                value={trangThaiFilter}
                onChange={(event) =>
                  setTrangThaiFilter(event.target.value as TrangThaiNguoiDungFilter)
                }
                className="h-11 w-full appearance-none rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 pr-11 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
              >
                {TRANG_THAI_FILTER_OPTIONS.map((option) => (
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
              <Label htmlFor="nguoi-dung-dia-chi-filter">Nhập địa chỉ hoặc khu vực</Label>
              <Input
                id="nguoi-dung-dia-chi-filter"
                type="text"
                placeholder="Nhập địa chỉ cần lọc"
                value={diaChiFilter}
                onChange={(event) => setDiaChiFilter(event.target.value)}
              />
            </div>

            <div>
              <Label htmlFor="nguoi-dung-rows-per-page">Số dòng mỗi trang</Label>
              <select
                id="nguoi-dung-rows-per-page"
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

          <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
            <div className="max-w-full overflow-x-auto custom-scrollbar">
              <Table className="min-w-[980px]">
                <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                  <TableRow>
                    <TableCell
                      isHeader
                      className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                    >
                      Tên
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                    >
                      Số điện thoại
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                    >
                      Địa chỉ
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                    >
                      Trạng thái kích hoạt
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-4 py-3 font-medium text-gray-500 text-center text-theme-xs dark:text-gray-400"
                    >
                      Hành động
                    </TableCell>
                  </TableRow>
                </TableHeader>

                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {paginatedNguoiDungList.map((nguoiDung) => {
                    const isSelected = selectedNguoiDungId === nguoiDung.id;
                    const isActivated = getTrangThai(
                      nguoiDung.id,
                      nguoiDung.trangThaiKichHoat
                    );

                    return (
                      <TableRow
                        key={nguoiDung.id}
                        onClick={() => handleOpenUserDetailDialog(nguoiDung.id)}
                        className={`cursor-pointer transition-colors ${isSelected
                            ? "bg-brand-50/70 dark:bg-brand-500/10"
                            : "hover:bg-gray-50 dark:hover:bg-white/[0.02]"
                          }`}
                      >
                        <TableCell className="px-5 py-4 sm:px-6 text-start">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 overflow-hidden rounded-full">
                              <img
                                width={40}
                                height={40}
                                src={nguoiDung.avatarUrl}
                                alt={nguoiDung.ten}
                                className="size-10 object-cover"
                              />
                            </div>
                            <span className="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                              {nguoiDung.ten}
                            </span>
                          </div>
                        </TableCell>

                        <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                          {nguoiDung.soDienThoai}
                        </TableCell>

                        <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                          {nguoiDung.diaChi}
                        </TableCell>

                        <TableCell className="px-4 py-3 text-start text-theme-sm dark:text-gray-400">
                          <div className="inline-flex min-w-[160px] flex-col gap-1">
                            <select
                              value={isActivated ? "active" : "inactive"}
                              disabled={Boolean(isUpdatingTrangThaiByNguoiDungId[nguoiDung.id])}
                              onClick={(event) => event.stopPropagation()}
                              onChange={(event) => {
                                event.stopPropagation();
                                const nextTrangThai = event.target.value === "active";
                                void handleChangeActivation(nguoiDung, nextTrangThai);
                              }}
                              className="h-9 w-full rounded-lg border border-gray-300 bg-white px-3 text-xs text-gray-700 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                            >
                              <option value="active">Đang hoạt động</option>
                              <option value="inactive">Tạm khóa</option>
                            </select>
                            {isUpdatingTrangThaiByNguoiDungId[nguoiDung.id] && (
                              <span className="text-xs text-gray-500 dark:text-gray-400">
                                Đang cập nhật...
                              </span>
                            )}
                          </div>
                        </TableCell>

                        <TableCell className="px-4 py-3">
                          <div className="flex items-center justify-center gap-2">
                            <button
                              type="button"
                              onClick={(event) => {
                                event.stopPropagation();
                                handleOpenUserDetailDialog(nguoiDung.id);
                              }}
                              className="inline-flex items-center justify-center w-8 h-8 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-100 hover:text-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                              aria-label={`Xem chi tiết ${nguoiDung.ten}`}
                            >
                              <EyeIcon className="fill-gray-500 dark:fill-gray-400 size-5" />
                            </button>
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}

                  {paginatedNguoiDungList.length === 0 && (
                    <TableRow>
                      <td
                        className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                        colSpan={5}
                      >
                        {totalFilteredNguoiDung === 0
                          ? "Không có người dùng phù hợp với bộ lọc."
                          : "Chưa có người dùng nào trong danh sách."}
                      </td>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          </div>

          <Pagination
            currentPage={currentPage}
            totalItems={totalFilteredNguoiDung}
            itemsPerPage={rowsPerPage}
            totalItemsOverall={nguoiDungList.length}
            onPageChange={setCurrentPage}
          />
        </ComponentCard>
      </div>

      <Modal
        isOpen={isUserDetailDialogOpen}
        onClose={handleCloseUserDetailDialog}
        className="max-w-[860px] m-4"
      >
        {selectedNguoiDung ? (
          <div className="p-6 sm:p-7">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-4">
                <div className="h-14 w-14 overflow-hidden rounded-full">
                  <img
                    src={selectedNguoiDung.avatarUrl}
                    alt={selectedNguoiDung.ten}
                    className="h-14 w-14 object-cover"
                  />
                </div>
                <div>
                  <h4 className="text-base font-semibold text-gray-800 dark:text-white/90">
                    {selectedNguoiDung.ten}
                  </h4>
                  <p className="text-theme-sm text-gray-500 dark:text-gray-400">
                    Mã người dùng: {selectedNguoiDung.id}
                  </p>
                </div>
              </div>
              <Badge
                color={
                  getTrangThai(
                    selectedNguoiDung.id,
                    selectedNguoiDung.trangThaiKichHoat
                  )
                    ? "success"
                    : "error"
                }
              >
                {getTrangThai(
                  selectedNguoiDung.id,
                  selectedNguoiDung.trangThaiKichHoat
                )
                  ? "Tài khoản đang hoạt động"
                  : "Tài khoản tạm khóa"}
              </Badge>
            </div>

            <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05]">
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  Số điện thoại
                </p>
                <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                  {selectedNguoiDung.soDienThoai}
                </p>
              </div>

              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05]">
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  Địa chỉ
                </p>
                <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                  {selectedNguoiDung.diaChi}
                </p>
              </div>

              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05]">
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  Tọa độ
                </p>
                <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                  {selectedNguoiDung.toaDo}
                </p>
              </div>

              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05]">
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  Tài khoản ID
                </p>
                <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                  {selectedNguoiDung.taiKhoanId ?? "Chưa cập nhật"}
                </p>
              </div>

              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05]">
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  Tên đăng nhập
                </p>
                <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                  {selectedNguoiDung.tenDangNhap}
                </p>
              </div>

              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05]">
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  Email
                </p>
                <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                  {selectedNguoiDung.email}
                </p>
              </div>

              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05]">
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  Tạo lúc
                </p>
                <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                  {formatDateTime(selectedNguoiDung.createdAt)}
                </p>
              </div>

              <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.05] sm:col-span-2 lg:col-span-3">
                <button
                  type="button"
                  onClick={() => setIsHoTroListOpen((prev) => !prev)}
                  className="flex w-full items-center justify-between gap-3 text-left"
                >
                  <div>
                    <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                      Tổng số phiếu cứu trợ
                    </p>
                    <p className="mt-1 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                      {selectedNguoiDung.tongSoPhieuHoTro}
                    </p>
                    <p className="mt-1 text-theme-xs text-brand-600 dark:text-brand-400">
                      Nhấn vào để xem lịch sử tạo phiếu cứu trợ
                    </p>
                  </div>
                  <span
                    className={`inline-flex items-center rounded-full border px-3 py-1 text-theme-xs font-medium ${isHoTroListOpen
                        ? "border-brand-300 bg-brand-50 text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300"
                        : "border-gray-200 text-gray-600 dark:border-white/[0.08] dark:text-gray-300"
                      }`}
                  >
                    {isHoTroListOpen ? "Thu gọn" : "Mở rộng"}
                  </span>
                </button>

                {isHoTroListOpen && (
                  <div className="mt-4 space-y-3 border-t border-gray-100 pt-4 dark:border-white/[0.06]">
                    {selectedNguoiDungHoTroList.length === 0 ? (
                      <div className="rounded-lg border border-dashed border-gray-300 p-4 text-theme-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
                        Chưa có lịch sử tạo phiếu cứu trợ
                      </div>
                    ) : (
                      selectedNguoiDungHoTroList.map((phieuHoTro) => {
                        const isExpanded = Boolean(expandedPhieuIds[phieuHoTro.id]);

                        return (
                          <div
                            key={phieuHoTro.id}
                            className="overflow-hidden rounded-lg border border-gray-200 dark:border-white/[0.08]"
                          >
                            <button
                              type="button"
                              onClick={() => handleTogglePhieuDropdown(phieuHoTro.id)}
                              className="flex w-full items-center justify-between gap-3 bg-gray-50 px-4 py-3 text-left hover:bg-gray-100 dark:bg-white/[0.02] dark:hover:bg-white/[0.04]"
                            >
                              <div>
                                <p className="text-theme-sm font-medium text-gray-800 dark:text-white/90">
                                  Phiếu #{phieuHoTro.id}
                                </p>
                                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                  {formatDateTime(phieuHoTro.thoiGianHoTro)}
                                </p>
                                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                  Trạng thái: {phieuHoTro.trangThaiPhieu}
                                </p>
                              </div>
                              <span className="text-theme-xs font-medium text-brand-600 dark:text-brand-400">
                                {isExpanded ? "Ẩn chi tiết" : "Xem chi tiết"}
                              </span>
                            </button>

                            {isExpanded && (
                              <div className="space-y-4 border-t border-gray-100 p-4 dark:border-white/[0.06]">
                                <div className="grid gap-2 sm:grid-cols-2">
                                  <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                    Người nhận:{" "}
                                    <span className="font-medium text-gray-800 dark:text-white/90">
                                      {phieuHoTro.nguoiNhan}
                                    </span>
                                  </p>
                                  <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                    Số điện thoại:{" "}
                                    <span className="font-medium text-gray-800 dark:text-white/90">
                                      {phieuHoTro.soDienThoai}
                                    </span>
                                  </p>
                                </div>

                                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                  Địa chỉ hỗ trợ:{" "}
                                  <span className="font-medium text-gray-800 dark:text-white/90">
                                    {phieuHoTro.diaChi}
                                  </span>
                                </p>

                                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                  Ghi chú:{" "}
                                  <span className="font-medium text-gray-800 dark:text-white/90">
                                    {phieuHoTro.ghiChu}
                                  </span>
                                </p>

                                <div>
                                  <p className="mb-2 text-theme-xs font-medium text-gray-700 dark:text-gray-300">
                                    Vật phẩm cần cứu trợ
                                  </p>
                                  <div className="space-y-2">
                                    {phieuHoTro.vatPhamHoTro.map((vatPham, index) => (
                                      <div
                                        key={`${phieuHoTro.id}-${vatPham.tenVatPham}-${index}`}
                                        className="rounded-md border border-gray-100 p-3 dark:border-white/[0.05]"
                                      >
                                        <p className="text-theme-sm font-medium text-gray-800 dark:text-white/90">
                                          {vatPham.tenVatPham}
                                        </p>
                                        <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                          Số lượng: {vatPham.soLuong}
                                        </p>
                                        <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                          Ghi chú: {vatPham.ghiChu}
                                        </p>
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              </div>
                            )}
                          </div>
                        );
                      })
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : (
          <div className="p-6 text-center text-theme-sm text-gray-500 dark:text-gray-400">
            Chưa có người dùng nào được chọn.
          </div>
        )}
      </Modal>
      <ActionToast toast={actionToast} onClose={() => setActionToast(null)} />
    </>
  );
}
