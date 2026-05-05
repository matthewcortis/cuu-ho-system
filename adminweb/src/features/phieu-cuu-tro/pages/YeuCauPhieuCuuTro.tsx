import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import ComponentCard from "@/components/common/ComponentCard";
import PageBreadCrumb from "@/components/common/PageBreadCrumb";
import PageMeta from "@/components/common/PageMeta";
import {
  AngleLeftIcon,
  AngleRightIcon,
  ChevronDownIcon,
  MoreDotIcon,
} from "@/icons";
import { Dropdown } from "@/components/ui/dropdown/Dropdown";
import { Modal } from "@/components/ui/modal";
import {
  dieuPhoiPhieu,
  fetchPhieuCuuTroList,
  NguoiDungApiError,
  type PhieuCuuTroDto,
  type PhieuCuuTroTepTinDto,
} from "@/features/nguoi-dung/api/nguoiDungApi";
import {
  DoiNhomApiError,
  fetchDoiNhomList,
  type DoiNhomDto,
} from "@/features/tinh-nguyen-vien/api/doiNhomApi";
import {
  fetchVatPhamList,
  type VatPhamDto,
  VatPhamApiError,
} from "@/features/vat-pham/api/vatPhamApi";

type PhieuMediaFile = {
  id: number;
  duongDan: string;
  loaiTepTin: string;
};

type PhieuVatPhamItem = {
  id: number;
  vatPhamId: number | null;
  ten: string;
  soLuong: string;
  imageUrl: string | null;
};

type TeamOption = {
  id: number;
  tenDoiNhom: string;
  soDienThoai: string;
  diaChi: string;
  dangHoatDong: boolean;
};

type PhieuCuuTroListItem = {
  id: number;
  loaiCuuTroTen: string;
  loaiCuuTroIconUrl: string;
  ghiChu: string;
  diaChi: string;
  toaDo: string;
  createdAt: string;
  hoTenNguoiCanHoTro: string;
  soDienThoaiNguoiCanHoTro: string;
  vatPhams: PhieuVatPhamItem[];
  mediaImages: string[];
  mediaFiles: PhieuMediaFile[];
  videoUrl: string | null;
};

const DEFAULT_CUU_TRO_ICON = "/images/icons/file-image.svg";
const REQUEST_VISIBLE_STATUSES = new Set(["CHO_DIEU_PHOI", "PENDING"]);

function trimOrFallback(value: string | null | undefined, fallback: string): string {
  if (typeof value !== "string") {
    return fallback;
  }

  const normalized = value.trim();
  return normalized.length > 0 ? normalized : fallback;
}

function getApiErrorMessage(error: unknown): string {
  if (
    error instanceof NguoiDungApiError ||
    error instanceof DoiNhomApiError ||
    error instanceof VatPhamApiError
  ) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the tai du lieu phieu cuu tro.";
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

function getUniqueNonEmptyValues(values: string[]): string[] {
  return values
    .map((value) => value.trim())
    .filter((value, index, array) => value.length > 0 && array.indexOf(value) === index);
}

function buildVatPhamListForPhieu(
  phieu: PhieuCuuTroDto,
  vatPhamImageUrlById: Map<number, string>
): PhieuVatPhamItem[] {
  if (phieu.chiTietCuuTro.length === 0) {
    return [
      {
        id: phieu.id,
        vatPhamId: null,
        ten: "Chua cap nhat vat pham",
        soLuong: "Chua cap nhat",
        imageUrl: null,
      },
    ];
  }

  return phieu.chiTietCuuTro.map((vatPham) => {
    const soLuongLabel =
      vatPham.soLuong !== null && vatPham.soLuong !== undefined
        ? String(vatPham.soLuong)
        : "Chua cap nhat";

    return {
      id: vatPham.id ?? phieu.id,
      vatPhamId: vatPham.vatPhamId ?? null,
      ten: trimOrFallback(vatPham.tenVatPham, "Chua cap nhat vat pham"),
      soLuong: soLuongLabel,
      imageUrl:
        typeof vatPham.vatPhamId === "number"
          ? vatPhamImageUrlById.get(vatPham.vatPhamId) ?? null
          : null,
    };
  });
}

function buildMediaFiles(tepTins: PhieuCuuTroTepTinDto[]): PhieuMediaFile[] {
  return tepTins
    .map((tepTinItem) => {
      const tepTin = tepTinItem.tepTin;
      if (!tepTin || !tepTin.duongDan.trim()) {
        return null;
      }

      return {
        id: tepTin.id,
        duongDan: tepTin.duongDan,
        loaiTepTin: trimOrFallback(tepTin.loaiTepTin, "application/octet-stream"),
      };
    })
    .filter((item): item is PhieuMediaFile => item !== null);
}

function buildVatPhamImageUrlById(vatPhamList: VatPhamDto[]): Map<number, string> {
  const map = new Map<number, string>();
  vatPhamList.forEach((item) => {
    const imageUrl = item.tepTin?.duongDan?.trim() ?? "";
    if (imageUrl.length > 0) {
      map.set(item.id, imageUrl);
    }
  });
  return map;
}

function buildPhieuCuuTroItems(
  phieuList: PhieuCuuTroDto[],
  vatPhamImageUrlById: Map<number, string>
): PhieuCuuTroListItem[] {
  return phieuList
    .map((phieu) => {
      const vatPhams = buildVatPhamListForPhieu(phieu, vatPhamImageUrlById);
      const mediaFiles = buildMediaFiles(phieu.tepTins);
      const apiImageUrls = mediaFiles
        .filter((item) => item.loaiTepTin.startsWith("image/"))
        .map((item) => item.duongDan);
      const mediaImages = getUniqueNonEmptyValues(apiImageUrls);
      const videoFile =
        mediaFiles.find((item) => item.loaiTepTin.startsWith("video/")) ?? null;
      const videoUrl = videoFile?.duongDan ?? null;

      return {
        id: phieu.id,
        loaiCuuTroTen: trimOrFallback(phieu.loaiSuCo?.ten, "Phieu cuu tro"),
        loaiCuuTroIconUrl: trimOrFallback(phieu.loaiSuCo?.iconUrl, DEFAULT_CUU_TRO_ICON),
        ghiChu: trimOrFallback(phieu.ghiChu, "Khong co ghi chu"),
        diaChi: trimOrFallback(phieu.viTri?.diaChi, "Chua cap nhat dia chi"),
        toaDo:
          phieu.viTri?.lat && phieu.viTri?.longitude
            ? `${phieu.viTri.lat}, ${phieu.viTri.longitude}`
            : "Chua cap nhat toa do",
        createdAt: phieu.createdAt,
        hoTenNguoiCanHoTro: trimOrFallback(phieu.nguoiGui?.ten, "Chua cap nhat"),
        soDienThoaiNguoiCanHoTro: trimOrFallback(phieu.nguoiGui?.sdt, "Chua cap nhat"),
        vatPhams,
        mediaImages,
        mediaFiles,
        videoUrl,
      };
    })
    .sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
    );
}

function buildTeamOptions(doiNhomList: DoiNhomDto[]): TeamOption[] {
  return doiNhomList.map((team) => ({
    id: team.id,
    tenDoiNhom: trimOrFallback(team.tenDoiNhom, `Doi #${team.id}`),
    soDienThoai: trimOrFallback(team.soDienThoai, "Chua cap nhat"),
    diaChi: trimOrFallback(team.viTri?.diaChi, "Chua cap nhat"),
    dangHoatDong: Boolean(team.trangThaiHoatDong),
  }));
}

function normalizeTrangThaiPhieu(value: string | null | undefined): string {
  if (typeof value !== "string") {
    return "";
  }
  return value.trim().toUpperCase();
}

function isChoDieuPhoiTrangThai(value: string | null | undefined): boolean {
  return REQUEST_VISIBLE_STATUSES.has(normalizeTrangThaiPhieu(value));
}

export default function YeuCauPhieuCuuTroPage() {
  const [expandedByPhieuId, setExpandedByPhieuId] = useState<Record<number, boolean>>({});
  const mediaTrackRefs = useRef<Record<number, HTMLDivElement | null>>({});
  const [openVatPhamMenuByPhieuId, setOpenVatPhamMenuByPhieuId] = useState<
    number | null
  >(null);
  const [openDetailMenuByPhieuId, setOpenDetailMenuByPhieuId] = useState<
    number | null
  >(null);
  const [isTeamDialogOpen, setIsTeamDialogOpen] = useState(false);
  const [selectedPhieuForTeam, setSelectedPhieuForTeam] =
    useState<PhieuCuuTroListItem | null>(null);
  const [teamAddressFilter, setTeamAddressFilter] = useState("");
  const [selectedTeamIdByPhieuId, setSelectedTeamIdByPhieuId] = useState<
    Record<number, number>
  >({});
  const [phieuApiList, setPhieuApiList] = useState<PhieuCuuTroDto[]>([]);
  const [doiNhomApiList, setDoiNhomApiList] = useState<DoiNhomDto[]>([]);
  const [vatPhamApiList, setVatPhamApiList] = useState<VatPhamDto[]>([]);
  const [isDataLoading, setIsDataLoading] = useState<boolean>(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [dispatchingPhieuId, setDispatchingPhieuId] = useState<number | null>(null);
  const [dispatchFeedback, setDispatchFeedback] = useState<string | null>(null);

  const loadPageData = useCallback(async (): Promise<void> => {
    setIsDataLoading(true);
    setApiError(null);

    try {
      const [phieuResult, doiNhomResult, vatPhamResult] = await Promise.all([
        fetchPhieuCuuTroList(),
        fetchDoiNhomList(),
        fetchVatPhamList(),
      ]);

      setPhieuApiList(phieuResult);
      setDoiNhomApiList(doiNhomResult);
      setVatPhamApiList(vatPhamResult);
    } catch (error) {
      setApiError(getApiErrorMessage(error));
      setPhieuApiList([]);
      setDoiNhomApiList([]);
      setVatPhamApiList([]);
    } finally {
      setIsDataLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadPageData();
  }, [loadPageData]);

  const vatPhamImageUrlById = useMemo(
    () => buildVatPhamImageUrlById(vatPhamApiList),
    [vatPhamApiList]
  );
  const phieuItems = useMemo(() => {
    const choDieuPhoiPhieuList = phieuApiList.filter((phieu) =>
      isChoDieuPhoiTrangThai(phieu.trangThai)
    );
    return buildPhieuCuuTroItems(choDieuPhoiPhieuList, vatPhamImageUrlById);
  }, [phieuApiList, vatPhamImageUrlById]);
  const teamOptions = useMemo<TeamOption[]>(
    () => buildTeamOptions(doiNhomApiList),
    [doiNhomApiList]
  );
  const filteredTeamOptions = useMemo(() => {
    const normalizedAddressKeyword = teamAddressFilter.trim().toLowerCase();
    if (!normalizedAddressKeyword) {
      return teamOptions;
    }

    return teamOptions.filter((team) =>
      team.diaChi.toLowerCase().includes(normalizedAddressKeyword)
    );
  }, [teamAddressFilter, teamOptions]);

  const toggleExpanded = (phieuId: number) => {
    setExpandedByPhieuId((prev) => ({
      ...prev,
      [phieuId]: !prev[phieuId],
    }));
    setOpenVatPhamMenuByPhieuId(null);
    setOpenDetailMenuByPhieuId(null);
  };

  const toggleVatPhamMenu = (phieuId: number) => {
    setOpenVatPhamMenuByPhieuId((prev) => (prev === phieuId ? null : phieuId));
  };

  const closeVatPhamMenu = () => {
    setOpenVatPhamMenuByPhieuId(null);
  };

  const toggleDetailMenu = (phieuId: number) => {
    setOpenDetailMenuByPhieuId((prev) => (prev === phieuId ? null : phieuId));
    setOpenVatPhamMenuByPhieuId(null);
  };

  const closeDetailMenu = () => {
    setOpenDetailMenuByPhieuId(null);
  };

  const handleOpenTeamDialog = (phieu: PhieuCuuTroListItem) => {
    setSelectedPhieuForTeam(phieu);
    setTeamAddressFilter("");
    setOpenVatPhamMenuByPhieuId(null);
    setOpenDetailMenuByPhieuId(null);
    setIsTeamDialogOpen(true);
  };

  const handleCloseTeamDialog = () => {
    setIsTeamDialogOpen(false);
    setSelectedPhieuForTeam(null);
    setTeamAddressFilter("");
  };

  const handleSelectTeam = (teamId: number) => {
    if (!selectedPhieuForTeam) return;

    setSelectedTeamIdByPhieuId((prev) => ({
      ...prev,
      [selectedPhieuForTeam.id]: teamId,
    }));
    handleCloseTeamDialog();
  };

  const handleDieuPhoiPhieu = async (phieuId: number, teamId: number) => {
    setDispatchingPhieuId(phieuId);
    setDispatchFeedback(null);

    try {
      await dieuPhoiPhieu(phieuId, teamId);
      setDispatchFeedback(`Da dieu phoi phieu #${phieuId} thanh cong.`);
      await loadPageData();
    } catch (error) {
      setDispatchFeedback(
        `Khong the dieu phoi phieu #${phieuId}: ${getApiErrorMessage(error)}`
      );
    } finally {
      setDispatchingPhieuId(null);
    }
  };

  const handleScrollImages = (phieuId: number, direction: "prev" | "next") => {
    const mediaTrack = mediaTrackRefs.current[phieuId];
    if (!mediaTrack) return;

    const firstCard = mediaTrack.querySelector(
      "[data-media-card='true']"
    ) as HTMLElement | null;
    const gap = 12;
    const scrollDistance = (firstCard?.offsetWidth ?? mediaTrack.clientWidth * 0.65) + gap;
    const delta = direction === "next" ? scrollDistance : -scrollDistance;

    mediaTrack.scrollBy({
      left: delta,
      behavior: "smooth",
    });
  };

  const handlePlayVideo = (videoUrl: string | null) => {
    if (!videoUrl) return;
    window.open(videoUrl, "_blank", "noopener,noreferrer");
  };

  const handleViewOnMap = (phieu: PhieuCuuTroListItem) => {
    const normalizedToaDo = phieu.toaDo.trim().toLowerCase();
    const hasValidCoordinates =
      normalizedToaDo.length > 0 && !normalizedToaDo.includes("chua cap nhat");
    const query = hasValidCoordinates ? phieu.toaDo : phieu.diaChi;

    window.open(
      `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(query)}`,
      "_blank",
      "noopener,noreferrer"
    );
    closeDetailMenu();
  };

  const fallbackCopyText = (text: string) => {
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.position = "fixed";
    textArea.style.opacity = "0";
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    document.execCommand("copy");
    document.body.removeChild(textArea);
  };

  const handleCopyPhieuInfo = async (phieu: PhieuCuuTroListItem) => {
    const content = [
      `Mã phiếu: #${phieu.id}`,
      `Tọa độ: ${phieu.toaDo}`,
      `Thời gian tạo: ${formatDateTime(phieu.createdAt)}`,
    ].join("\n");

    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(content);
      } else {
        fallbackCopyText(content);
      }
    } catch {
      fallbackCopyText(content);
    }

    closeDetailMenu();
  };

  return (
    <>
      <PageMeta
        title="Yêu cầu cứu trợ"
        description="Danh sách các yêu cầu cứu trợ."
      />
      <PageBreadCrumb pageTitle="Yêu cầu phiếu cứu trợ" />

      <div className="space-y-6">
        <ComponentCard
          title="Danh sách phiếu cứu trợ"
          desc=""
        >
          <div className="space-y-4">
            {isDataLoading && (
              <div className="rounded-lg border border-dashed border-gray-300 p-5 text-center text-theme-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
                Dang tai du lieu phieu cuu tro...
              </div>
            )}

            {!isDataLoading && apiError && (
              <div className="rounded-lg border border-danger-200 bg-danger-50 p-5 text-center text-theme-sm text-danger-700 dark:border-danger-500/20 dark:bg-danger-500/10 dark:text-danger-400">
                {apiError}
              </div>
            )}

            {!isDataLoading && !apiError && dispatchFeedback && (
              <div className="rounded-lg border border-brand-200 bg-brand-50 p-4 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
                {dispatchFeedback}
              </div>
            )}

            {!isDataLoading && !apiError && phieuItems.map((phieuItem) => {
              const isExpanded = Boolean(expandedByPhieuId[phieuItem.id]);
              const summaryImages = phieuItem.mediaImages.slice(0, 2);
              const hasSummaryImages = summaryImages.length > 0;
              const vatPhamDauTien = phieuItem.vatPhams[0];
              const soVatPhamConLai = Math.max(0, phieuItem.vatPhams.length - 1);
              const hasVideo = Boolean(phieuItem.videoUrl);
              const hasMediaImages = phieuItem.mediaImages.length > 0;
              const selectedTeam =
                teamOptions.find(
                  (team) => team.id === selectedTeamIdByPhieuId[phieuItem.id]
                ) ?? null;

              return (
                <article
                  key={phieuItem.id}
                  className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.08] dark:bg-white/[0.03]"
                >
                  <div className="flex items-center justify-between gap-3 p-4 sm:p-5">
                    <div className="flex min-w-0 items-center gap-3">
                      <div className="h-12 w-12 shrink-0 overflow-hidden rounded-full border border-gray-200 bg-gray-50 dark:border-white/[0.08] dark:bg-white/[0.04]">
                        <img
                          src={phieuItem.loaiCuuTroIconUrl}
                          alt={phieuItem.loaiCuuTroTen}
                          className="h-full w-full object-cover"
                        />
                      </div>

                      <div className="min-w-0">
                        <p className="truncate text-sm font-semibold text-gray-800 dark:text-white/90">
                          {phieuItem.loaiCuuTroTen}
                        </p>
                        <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                          Mã phiếu #{phieuItem.id} · {formatDateTime(phieuItem.createdAt)}
                        </p>
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => toggleExpanded(phieuItem.id)}
                      className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-gray-200 text-gray-700 transition-colors hover:bg-gray-50 dark:border-white/[0.08] dark:text-gray-200 dark:hover:bg-white/[0.05]"
                      aria-label={isExpanded ? "Đóng" : "Mở"}
                    >
                      <ChevronDownIcon
                        className={`size-5 transition-transform duration-300 ease-out ${
                          isExpanded ? "rotate-180" : "rotate-0"
                        }`}
                      />
                    </button>
                  </div>

                  <div className="grid gap-4 border-t border-gray-100 p-4 sm:p-5 lg:grid-cols-4 dark:border-white/[0.06]">
                    <div className="space-y-3 rounded-lg border border-gray-100 p-3 lg:col-span-2 dark:border-white/[0.08]">
                      <div>
                        <p className="text-theme-xs text-gray-500 dark:text-gray-400">Ghi chú</p>
                        <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                          {phieuItem.ghiChu}
                        </p>
                      </div>
                      <div>
                        <p className="text-theme-xs text-gray-500 dark:text-gray-400">Địa chỉ</p>
                        <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                          {phieuItem.diaChi}
                        </p>
                      </div>
                    </div>

                    <div className="lg:col-span-2">
                      {hasSummaryImages ? (
                        <div className="grid grid-cols-2 gap-3">
                          {summaryImages.map((imageUrl, imageIndex) => (
                            <div
                              key={`${phieuItem.id}-${imageUrl}-${imageIndex}`}
                              className="overflow-hidden rounded-lg border border-gray-100 dark:border-white/[0.08]"
                            >
                              <img
                                src={imageUrl}
                                alt={`Tinh hinh ${imageIndex + 1} cua phieu ${phieuItem.id}`}
                                className="h-28 w-full object-cover"
                              />
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="flex h-28 items-center justify-center rounded-lg border border-dashed border-gray-200 text-theme-xs text-gray-500 dark:border-white/[0.08] dark:text-gray-400">
                          Chua co anh hien truong
                        </div>
                      )}
                    </div>
                  </div>

                  <div
                    className={`grid transition-all duration-300 ease-out ${
                      isExpanded
                        ? "grid-rows-[1fr] opacity-100"
                        : "pointer-events-none grid-rows-[0fr] opacity-0"
                    }`}
                  >
                    <div className="overflow-hidden">
                      <div className="space-y-4 border-t border-gray-100 p-4 sm:p-5 dark:border-white/[0.06]">
                        <div className="grid gap-3 md:grid-cols-3">
                          <div className="relative rounded-lg border border-gray-100 p-3 dark:border-white/[0.08]">
                            <div className="flex items-start justify-between gap-3">
                              <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                Chi tiết phiếu
                              </p>
                              <button
                                type="button"
                                className="dropdown-toggle inline-flex h-7 w-7 items-center justify-center rounded-lg border border-gray-200 text-gray-500 transition-colors hover:bg-gray-50 hover:text-gray-700 dark:border-white/[0.08] dark:text-gray-300 dark:hover:bg-white/[0.05]"
                                onClick={() => toggleDetailMenu(phieuItem.id)}
                                aria-label={`Mo lua chon chi tiet phieu ${phieuItem.id}`}
                              >
                                <MoreDotIcon className="size-4" />
                              </button>
                              <Dropdown
                                isOpen={openDetailMenuByPhieuId === phieuItem.id}
                                onClose={closeDetailMenu}
                                className="w-52 p-3"
                              >
                                <p className="text-theme-xs font-medium text-gray-500 dark:text-gray-400">
                                  Lựa chọn
                                </p>
                                <div className="mt-2 space-y-1">
                                  <button
                                    type="button"
                                    onClick={() => handleViewOnMap(phieuItem)}
                                    className="w-full rounded-lg border border-gray-100 px-3 py-2 text-left text-theme-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-white/[0.08] dark:text-gray-200 dark:hover:bg-white/[0.05]"
                                  >
                                    Xem trên bản đồ
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => {
                                      void handleCopyPhieuInfo(phieuItem);
                                    }}
                                    className="w-full rounded-lg border border-gray-100 px-3 py-2 text-left text-theme-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-white/[0.08] dark:text-gray-200 dark:hover:bg-white/[0.05]"
                                  >
                                    Coppy
                                  </button>
                                </div>
                              </Dropdown>
                            </div>
                            <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                              Mã phiếu: #{phieuItem.id}
                            </p>
                            <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                              Tọa độ: {phieuItem.toaDo}
                            </p>
                            <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                              Thời gian tạo: {formatDateTime(phieuItem.createdAt)}
                            </p>
                          </div>

                          <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.08]">
                            <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                              Người cần hỗ trợ
                            </p>
                            <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                              {phieuItem.hoTenNguoiCanHoTro}
                            </p>
                            <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                              SĐT: {phieuItem.soDienThoaiNguoiCanHoTro}
                            </p>
                            <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                              Địa chỉ: {phieuItem.diaChi}
                            </p>
                          </div>

                          <div className="relative rounded-lg border border-gray-100 p-3 dark:border-white/[0.08]">
                            <div className="flex items-start justify-between gap-3">
                              <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                Danh sách vật phẩm
                              </p>

                              <button
                                type="button"
                                className="dropdown-toggle inline-flex h-7 w-7 items-center justify-center rounded-lg border border-gray-200 text-gray-500 transition-colors hover:bg-gray-50 hover:text-gray-700 dark:border-white/[0.08] dark:text-gray-300 dark:hover:bg-white/[0.05]"
                                onClick={() => toggleVatPhamMenu(phieuItem.id)}
                                aria-label={`Mo menu danh sach vat pham cua phieu ${phieuItem.id}`}
                              >
                                <MoreDotIcon className="size-4" />
                              </button>

                              <Dropdown
                                isOpen={openVatPhamMenuByPhieuId === phieuItem.id}
                                onClose={closeVatPhamMenu}
                                className="w-64 p-3"
                              >
                                <p className="text-theme-xs font-medium text-gray-500 dark:text-gray-400">
                                  Danh sách vật phẩm
                                </p>
                                <div className="mt-2 space-y-2">
                                  {phieuItem.vatPhams.map((vatPham) => (
                                    <div
                                      key={`${phieuItem.id}-${vatPham.id}`}
                                      className="flex items-start gap-2 rounded-lg border border-gray-100 p-2 dark:border-white/[0.08]"
                                    >
                                      <div className="h-10 w-10 shrink-0 overflow-hidden rounded-md border border-gray-200 bg-gray-50 dark:border-white/[0.08] dark:bg-white/[0.05]">
                                        {vatPham.imageUrl ? (
                                          <img
                                            src={vatPham.imageUrl}
                                            alt={vatPham.ten}
                                            className="h-full w-full object-cover"
                                          />
                                        ) : (
                                          <div className="flex h-full w-full items-center justify-center text-[10px] text-gray-400">
                                            Chua co anh
                                          </div>
                                        )}
                                      </div>
                                      <div className="min-w-0">
                                        <p className="text-sm font-medium text-gray-800 dark:text-white/90">
                                          {vatPham.ten}
                                        </p>
                                        <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                                          Số lượng: {vatPham.soLuong}
                                        </p>
                                      </div>
                                    </div>
                                  ))}
                                </div>
                              </Dropdown>
                            </div>

                            <div className="mt-2 flex items-start gap-2">
                              <div className="h-11 w-11 shrink-0 overflow-hidden rounded-md border border-gray-200 bg-gray-50 dark:border-white/[0.08] dark:bg-white/[0.05]">
                                {vatPhamDauTien?.imageUrl ? (
                                  <img
                                    src={vatPhamDauTien.imageUrl}
                                    alt={vatPhamDauTien.ten}
                                    className="h-full w-full object-cover"
                                  />
                                ) : (
                                  <div className="flex h-full w-full items-center justify-center text-[10px] text-gray-400">
                                    Chua co anh
                                  </div>
                                )}
                              </div>
                              <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                                {vatPhamDauTien?.ten ?? "Chua cap nhat vat pham"}
                              </p>
                            </div>
                            <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                              Số lượng: {vatPhamDauTien?.soLuong ?? "Chua cap nhat"}
                            </p>
                            {soVatPhamConLai > 0 && (
                              <p className="mt-1 text-theme-xs font-medium text-brand-600 dark:text-brand-400">
                                +{soVatPhamConLai} vật phẩm khác
                              </p>
                            )}
                          </div>
                        </div>

                        <div className="rounded-lg border border-gray-100 p-3 dark:border-white/[0.08]">
                          <p className="mb-3 text-theme-xs text-gray-500 dark:text-gray-400">
                            Media
                          </p>

                          {hasMediaImages || hasVideo ? (
                            <div className="grid gap-3 md:grid-cols-4 md:items-stretch">
                              {hasMediaImages && (
                                <div className={hasVideo ? "md:col-span-3" : "md:col-span-4"}>
                                  <div className="relative overflow-hidden rounded-lg border border-gray-100 bg-gray-50 px-10 py-3 dark:border-white/[0.08] dark:bg-white/[0.04]">
                                    <div
                                      ref={(node) => {
                                        mediaTrackRefs.current[phieuItem.id] = node;
                                      }}
                                      className="flex gap-3 overflow-x-auto scroll-smooth [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
                                    >
                                      {phieuItem.mediaImages.map((imageUrl, imageIndex) => (
                                        <a
                                          key={`${phieuItem.id}-media-${imageUrl}-${imageIndex}`}
                                          data-media-card="true"
                                          href={imageUrl}
                                          target="_blank"
                                          rel="noreferrer"
                                          className="block w-[45%] min-w-[170px] shrink-0 overflow-hidden rounded-md transition-transform duration-300 hover:-translate-y-0.5 sm:min-w-[200px] md:min-w-[220px]"
                                        >
                                          <div className="aspect-[2/1]">
                                            <img
                                              src={imageUrl}
                                              alt={`Danh sach anh ${imageIndex + 1} cua phieu ${phieuItem.id}`}
                                              className="h-full w-full object-cover"
                                            />
                                          </div>
                                        </a>
                                      ))}
                                    </div>

                                    {phieuItem.mediaImages.length > 1 && (
                                      <>
                                        <button
                                          type="button"
                                          onClick={() => handleScrollImages(phieuItem.id, "prev")}
                                          className="absolute left-2 top-1/2 inline-flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-white text-black transition-all duration-200 hover:scale-105 hover:bg-gray-100 active:scale-95"
                                          aria-label={`Xem anh truoc cua phieu ${phieuItem.id}`}
                                        >
                                          <AngleLeftIcon className="size-4" />
                                        </button>

                                        <button
                                          type="button"
                                          onClick={() => handleScrollImages(phieuItem.id, "next")}
                                          className="absolute right-2 top-1/2 inline-flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-white text-black transition-all duration-200 hover:scale-105 hover:bg-gray-100 active:scale-95"
                                          aria-label={`Xem anh tiep theo cua phieu ${phieuItem.id}`}
                                        >
                                          <AngleRightIcon className="size-4" />
                                        </button>
                                      </>
                                    )}
                                  </div>
                                </div>
                              )}

                              {hasVideo && (
                                <div className={hasMediaImages ? "md:col-span-1" : "md:col-span-4"}>
                                  <button
                                    type="button"
                                    onClick={() => handlePlayVideo(phieuItem.videoUrl)}
                                    className="relative flex h-full min-h-[120px] w-full items-center justify-center overflow-hidden rounded-lg border border-gray-100 bg-gray-900 text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-black dark:border-white/[0.08]"
                                  >
                                    <div className="flex flex-col items-center gap-2">
                                      <span className="inline-flex h-10 w-20 items-center justify-center rounded-full bg-white/90 text-sm font-semibold text-gray-900">
                                        Play
                                      </span>
                                      <span className="text-theme-xs text-white/85">
                                        Click de xem video
                                      </span>
                                    </div>
                                  </button>
                                </div>
                              )}
                            </div>
                          ) : (
                            <div className="rounded-lg border border-dashed border-gray-200 p-4 text-theme-xs text-gray-500 dark:border-white/[0.08] dark:text-gray-400">
                              Chua co media dinh kem.
                            </div>
                          )}

                          {selectedTeam && (
                            <div className="mt-3 rounded-lg border border-brand-200 bg-brand-50/60 p-3 dark:border-brand-500/30 dark:bg-brand-500/10">
                              <div className="flex flex-wrap items-start justify-between gap-3">
                                <div>
                                  <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                                    Đội xử lý đã chọn
                                  </p>
                                  <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                                    {selectedTeam.tenDoiNhom}
                                  </p>
                                  <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                                    SĐT: {selectedTeam.soDienThoai}
                                  </p>
                                  <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                                    Địa chỉ: {selectedTeam.diaChi}
                                  </p>
                                </div>

                                <button
                                  type="button"
                                  onClick={() => handleOpenTeamDialog(phieuItem)}
                                  className="inline-flex items-center rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-theme-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-white/[0.08] dark:bg-transparent dark:text-gray-200 dark:hover:bg-white/[0.05]"
                                >
                                  Chọn lại
                                </button>
                              </div>
                            </div>
                          )}

                          <div className="mt-3 flex flex-wrap items-center gap-2">
                            <button
                              type="button"
                              onClick={() => {
                                if (!selectedTeam) {
                                  handleOpenTeamDialog(phieuItem);
                                  return;
                                }

                                void handleDieuPhoiPhieu(phieuItem.id, selectedTeam.id);
                              }}
                              disabled={dispatchingPhieuId === phieuItem.id}
                              className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-brand-600"
                            >
                              {dispatchingPhieuId === phieuItem.id
                                ? "Dang dieu phoi..."
                                : selectedTeam
                                  ? "Xác nhận"
                                  : "Xử lý phiếu này"}
                            </button>
                          </div>

                          {/* {phieuItem.mediaFiles.length > 0 && (
                            <div className="mt-3 flex flex-wrap items-center gap-2">
                              {phieuItem.mediaFiles.map((mediaFile) => (
                                <a
                                  key={mediaFile.id}
                                  href={mediaFile.duongDan}
                                  target="_blank"
                                  rel="noreferrer"
                                  className="inline-flex items-center rounded-lg border border-gray-200 px-3 py-1.5 text-theme-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-white/[0.08] dark:text-gray-200 dark:hover:bg-white/[0.05]"
                                >
                                  Tep dinh kem ({mediaFile.loaiTepTin})
                                </a>
                              ))}
                            </div>
                          )} */}
                        </div>
                      </div>
                    </div>
                  </div>
                </article>
              );
            })}

            {!isDataLoading && !apiError && phieuItems.length === 0 && (
              <div className="rounded-lg border border-dashed border-gray-300 p-5 text-center text-theme-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
                Chua co phieu cuu tro nao.
              </div>
            )}
          </div>
        </ComponentCard>
      </div>

      <Modal
        isOpen={isTeamDialogOpen}
        onClose={handleCloseTeamDialog}
        className="max-w-[860px] m-4"
      >
        <div className="p-6 sm:p-7">
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Chọn đội nhóm xử lý phiếu
          </h3>
          <p className="mt-1 text-theme-sm text-gray-500 dark:text-gray-400">
            Chọn một đội để xử lý phiếu cứu trợ và lọc theo địa chỉ khi cần.
          </p>

          {selectedPhieuForTeam && (
            <div className="mt-4 rounded-xl border border-gray-200 bg-gray-50 p-4 dark:border-white/[0.08] dark:bg-white/[0.03]">
              <p className="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                Phiếu #{selectedPhieuForTeam.id} · {selectedPhieuForTeam.hoTenNguoiCanHoTro}
              </p>
              <p className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                SĐT: {selectedPhieuForTeam.soDienThoaiNguoiCanHoTro}
              </p>
              <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                Địa chỉ cần hỗ trợ: {selectedPhieuForTeam.diaChi}
              </p>
            </div>
          )}

          <div className="mt-4">
            <label
              htmlFor="doi-nhom-dia-chi-filter"
              className="mb-2 block text-theme-xs font-medium text-gray-600 dark:text-gray-300"
            >
              Lọc đội nhóm theo địa chỉ
            </label>
            <input
              id="doi-nhom-dia-chi-filter"
              type="text"
              value={teamAddressFilter}
              onChange={(event) => setTeamAddressFilter(event.target.value)}
              placeholder="Nhập địa chỉ đội nhóm cần lọc"
              className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
            />
          </div>

          <div className="mt-4 max-h-[420px] overflow-y-auto pr-1 custom-scrollbar">
            <div className="grid gap-3 sm:grid-cols-2">
              {filteredTeamOptions.map((team) => {
                const currentSelectedTeamId = selectedPhieuForTeam
                  ? selectedTeamIdByPhieuId[selectedPhieuForTeam.id]
                  : undefined;
                const isCurrentTeam = currentSelectedTeamId === team.id;

                return (
                  <div
                    key={team.id}
                    className={`rounded-xl border p-4 ${
                      isCurrentTeam
                        ? "border-brand-300 bg-brand-50/70 dark:border-brand-500/40 dark:bg-brand-500/10"
                        : "border-gray-200 bg-white dark:border-white/[0.08] dark:bg-white/[0.02]"
                    }`}
                  >
                    <p className="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                      {team.tenDoiNhom}
                    </p>
                    <p className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                      SĐT: {team.soDienThoai}
                    </p>
                    <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                      Địa chỉ: {team.diaChi}
                    </p>
                    <span
                      className={`mt-2 inline-flex rounded-full border px-2.5 py-1 text-theme-xs font-medium ${
                        team.dangHoatDong
                          ? "border-success-300 bg-success-50 text-success-700 dark:border-success-500/30 dark:bg-success-500/15 dark:text-success-400"
                          : "border-gray-300 bg-gray-50 text-gray-600 dark:border-gray-600 dark:bg-gray-700/40 dark:text-gray-300"
                      }`}
                    >
                      {team.dangHoatDong ? "Đang hoạt động" : "Tạm ngưng"}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleSelectTeam(team.id)}
                      className={`mt-3 inline-flex w-full items-center justify-center rounded-lg px-3 py-2 text-theme-xs font-medium ${
                        isCurrentTeam
                          ? "bg-brand-100 text-brand-700 dark:bg-brand-500/20 dark:text-brand-300"
                          : "bg-brand-500 text-white hover:bg-brand-600"
                      }`}
                    >
                      {isCurrentTeam ? "Đang chọn" : "Chọn đội này"}
                    </button>
                  </div>
                );
              })}

              {filteredTeamOptions.length === 0 && (
                <div className="sm:col-span-2 rounded-xl border border-dashed border-gray-300 p-5 text-center text-theme-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
                  Khong tim thay doi nhom phu hop voi dia chi da nhap.
                </div>
              )}
            </div>
          </div>
        </div>
      </Modal>
    </>
  );
}
