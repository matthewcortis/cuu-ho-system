import { useMemo, useRef, useState } from "react";
import ComponentCard from "../../components/common/ComponentCard";
import PageBreadCrumb from "../../components/common/PageBreadCrumb";
import PageMeta from "../../components/common/PageMeta";
import { mockDatabase } from "../../data";
import {
  AngleLeftIcon,
  AngleRightIcon,
  ChevronDownIcon,
  MoreDotIcon,
} from "../../icons";
import { Dropdown } from "../../components/ui/dropdown/Dropdown";
import { Modal } from "../../components/ui/modal";

type PhieuMediaFile = {
  id: number;
  duongDan: string;
  loaiTepTin: string;
};

type PhieuVatPhamItem = {
  id: number;
  ten: string;
  soLuong: string;
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
  videoPosterUrl: string;
};

const DEFAULT_CUU_TRO_ICON = "/images/icons/file-image.svg";
const DEFAULT_MEDIA_IMAGES = [
  "/images/lulut.jpeg",
  "/images/lulut1.jpeg",
  "/images/lulut2.jpeg",
];
const DEFAULT_VIDEO_POSTER = "/images/video-thumb/thumb-16.png";

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

function getImageSlots(index: number, imageFromFile: string | null): string[] {
  const seededImages = imageFromFile ? [imageFromFile] : [];
  let cursor = 0;

  while (seededImages.length < 3) {
    const imageIndex = (index + cursor) % DEFAULT_MEDIA_IMAGES.length;
    const imageUrl = DEFAULT_MEDIA_IMAGES[imageIndex];

    if (!seededImages.includes(imageUrl)) {
      seededImages.push(imageUrl);
    }

    cursor += 1;
  }

  return seededImages.slice(0, 3);
}

function buildVatPhamListForPhieu(
  preferredVatPhamId: number | null | undefined,
  seedIndex: number
): PhieuVatPhamItem[] {
  if (mockDatabase.vat_pham.length === 0) {
    return [
      {
        id: -1,
        ten: "Chua cap nhat vat pham",
        soLuong: "Chua cap nhat",
      },
    ];
  }

  const preferredIndex = mockDatabase.vat_pham.findIndex(
    (item) => item.id === preferredVatPhamId
  );
  const startIndex =
    preferredIndex >= 0 ? preferredIndex : seedIndex % mockDatabase.vat_pham.length;
  const selectedVatPhams: typeof mockDatabase.vat_pham = [];

  for (let offset = 0; offset < 3; offset += 1) {
    const vatPham = mockDatabase.vat_pham[
      (startIndex + offset) % mockDatabase.vat_pham.length
    ];

    if (!selectedVatPhams.some((item) => item.id === vatPham.id)) {
      selectedVatPhams.push(vatPham);
    }
  }

  return selectedVatPhams.map((vatPham) => {
    const donVi = mockDatabase.don_vi.find((item) => item.id === vatPham.don_vi_id);
    const soLuongLabel =
      vatPham.so_luong !== null && vatPham.so_luong !== undefined
        ? `${vatPham.so_luong}${donVi?.ten ? ` ${donVi.ten}` : ""}`
        : "Chua cap nhat";

    return {
      id: vatPham.id,
      ten: vatPham.ten_vat_pham ?? "Chua cap nhat vat pham",
      soLuong: soLuongLabel,
    };
  });
}

function buildPhieuCuuTroItems(): PhieuCuuTroListItem[] {
  return mockDatabase.phieu_cuu_tro
    .map((phieu, index) => {
      const danhSachCuuTro = mockDatabase.danh_sach_cuu_tro.find(
        (item) => item.id === phieu.danh_sach_cuu_tro_id
      );
      const viTri = mockDatabase.vi_tri.find((item) => item.id === phieu.vi_tri_id);
      const vatPhams = buildVatPhamListForPhieu(danhSachCuuTro?.vat_pham_id, index);
      const tepTin = mockDatabase.tep_tin.find((item) => item.id === phieu.tep_tin_id);

      const imageFromFile =
        tepTin?.duong_dan && tepTin.loai_tep_tin?.startsWith("image/")
          ? tepTin.duong_dan
          : null;
      const mediaImages = getImageSlots(index, imageFromFile);
      const mediaFiles: PhieuMediaFile[] =
        tepTin?.duong_dan && tepTin.loai_tep_tin
          ? [
              {
                id: tepTin.id,
                duongDan: tepTin.duong_dan,
                loaiTepTin: tepTin.loai_tep_tin,
              },
            ]
          : [];
      const videoFile =
        mediaFiles.find((item) => item.loaiTepTin.startsWith("video/")) ?? null;
      const videoUrl = videoFile?.duongDan ?? null;

      return {
        id: phieu.id,
        loaiCuuTroTen: danhSachCuuTro?.ten ?? "Loai cuu tro chua cap nhat",
        loaiCuuTroIconUrl: danhSachCuuTro?.icon_url ?? DEFAULT_CUU_TRO_ICON,
        ghiChu: phieu.ghi_chu ?? "Khong co ghi chu",
        diaChi: viTri?.dia_chi ?? "Chua cap nhat dia chi",
        toaDo:
          viTri?.lat && viTri.long ? `${viTri.lat}, ${viTri.long}` : "Chua cap nhat toa do",
        createdAt: phieu.created_at,
        hoTenNguoiCanHoTro: phieu.ho_ten ?? "Chua cap nhat",
        soDienThoaiNguoiCanHoTro: phieu.sdt ?? "Chua cap nhat",
        vatPhams,
        mediaImages,
        mediaFiles,
        videoUrl,
        videoPosterUrl: mediaImages[0] ?? DEFAULT_VIDEO_POSTER,
      };
    })
    .sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
    );
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

  const phieuItems = useMemo(() => buildPhieuCuuTroItems(), []);
  const teamOptions = useMemo<TeamOption[]>(
    () =>
      mockDatabase.doi_nhom.map((team) => {
        const viTri = mockDatabase.vi_tri.find((item) => item.id === team.vi_tri_id);
        return {
          id: team.id,
          tenDoiNhom: team.ten_doi_nhom,
          soDienThoai: team.so_dien_thoai ?? "Chua cap nhat",
          diaChi: viTri?.dia_chi ?? "Chua cap nhat",
          dangHoatDong: Boolean(team.trang_thai_hoat_dong),
        };
      }),
    []
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
            {phieuItems.map((phieuItem) => {
              const isExpanded = Boolean(expandedByPhieuId[phieuItem.id]);
              const summaryImages = phieuItem.mediaImages.slice(0, 2);
              const vatPhamDauTien = phieuItem.vatPhams[0];
              const soVatPhamConLai = Math.max(0, phieuItem.vatPhams.length - 1);
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

                    <div className="grid grid-cols-2 gap-3 lg:col-span-2">
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
                                      className="rounded-lg border border-gray-100 p-2 dark:border-white/[0.08]"
                                    >
                                      <p className="text-sm font-medium text-gray-800 dark:text-white/90">
                                        {vatPham.ten}
                                      </p>
                                      <p className="mt-1 text-theme-xs text-gray-600 dark:text-gray-300">
                                        Số lượng: {vatPham.soLuong}
                                      </p>
                                    </div>
                                  ))}
                                </div>
                              </Dropdown>
                            </div>

                            <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                              {vatPhamDauTien?.ten ?? "Chua cap nhat vat pham"}
                            </p>
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

                          <div className="grid gap-3 md:grid-cols-4 md:items-stretch">
                            <div className="md:col-span-3">
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
                              </div>
                            </div>

                            <div className="md:col-span-1">
                              <div className="relative h-full min-h-[120px] overflow-hidden rounded-lg border border-gray-100 bg-gray-900 dark:border-white/[0.08]">
                                <img
                                  src={phieuItem.videoPosterUrl}
                                  alt={`Video hien truong cua phieu ${phieuItem.id}`}
                                  className="h-full w-full object-cover opacity-70"
                                />
                                <div className="absolute inset-0 bg-black/30" />

                                <button
                                  type="button"
                                  onClick={() => handlePlayVideo(phieuItem.videoUrl)}
                                  disabled={!phieuItem.videoUrl}
                                  className="absolute inset-0 m-auto inline-flex h-10 w-20 items-center justify-center rounded-full bg-white/90 text-sm font-semibold text-gray-900 transition-all duration-200 hover:-translate-y-0.5 hover:bg-white disabled:cursor-not-allowed disabled:opacity-70"
                                >
                                  Play
                                </button>
                              </div>
                            </div>
                          </div>

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
                                }
                              }}
                              className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-brand-600"
                            >
                              {selectedTeam ? "Xác nhận" : "Xử lý phiếu này"}
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

            {phieuItems.length === 0 && (
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
