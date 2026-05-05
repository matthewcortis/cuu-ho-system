import { useCallback, useEffect, useMemo, useState } from "react";
import ComponentCard from "@/components/common/ComponentCard";
import PageBreadCrumb from "@/components/common/PageBreadCrumb";
import PageMeta from "@/components/common/PageMeta";
import { ChevronDownIcon } from "@/icons";
import {
  fetchPhieuCuuTroList,
  NguoiDungApiError,
  type PhieuCuuTroDto,
} from "@/features/nguoi-dung/api/nguoiDungApi";

type TrangThaiLichSuPhieu =
  | "DA_NHAN"
  | "DANG_TREN_DUONG_TOI"
  | "DANG_XU_LY"
  | "HOAN_THANH"
  | "HUY";

type HistoryProgressStepKey =
  | "DA_NHAN"
  | "DANG_TREN_DUONG_TOI"
  | "DANG_XU_LY"
  | "HOAN_THANH";

type LichSuCuuTroItem = {
  id: number;
  loaiCuuTroTen: string;
  loaiCuuTroIconUrl: string;
  createdAt: string;
  hoTenNguoiCanHoTro: string;
  soDienThoaiNguoiCanHoTro: string;
  diaChiNguoiCanHoTro: string;
  ghiChu: string;
  currentStatus: TrangThaiLichSuPhieu;
};

const DEFAULT_CUU_TRO_ICON = "/images/icons/file-image.svg";
const HISTORY_PROGRESS_STEPS: Array<{ key: HistoryProgressStepKey; label: string }> = [
  { key: "DA_NHAN", label: "Đã nhận" },
  { key: "DANG_TREN_DUONG_TOI", label: "Đang tới" },
  { key: "DANG_XU_LY", label: "Đang xử lý" },
  { key: "HOAN_THANH", label: "Hoàn thành" },
];
const HISTORY_VISIBLE_STATUSES = new Set<TrangThaiLichSuPhieu>([
  "DA_NHAN",
  "DANG_TREN_DUONG_TOI",
  "DANG_XU_LY",
  "HOAN_THANH",
  "HUY",
]);

function trimOrFallback(value: string | null | undefined, fallback: string): string {
  if (typeof value !== "string") {
    return fallback;
  }

  const normalized = value.trim();
  return normalized.length > 0 ? normalized : fallback;
}

function normalizeTrangThaiPhieu(value: string | null | undefined): string {
  if (typeof value !== "string") {
    return "";
  }

  return value.trim().toUpperCase();
}

function toLichSuTrangThai(value: string | null | undefined): TrangThaiLichSuPhieu | null {
  const normalized = normalizeTrangThaiPhieu(value);

  if (normalized === "ASSIGNED" || normalized === "ACCEPTED") {
    return "DA_NHAN";
  }
  if (normalized === "IN_PROGRESS" || normalized === "PROCESSING") {
    return "DANG_XU_LY";
  }
  if (normalized === "COMPLETED" || normalized === "DONE") {
    return "HOAN_THANH";
  }
  if (normalized === "CANCELLED" || normalized === "CANCELED") {
    return "HUY";
  }

  if (HISTORY_VISIBLE_STATUSES.has(normalized as TrangThaiLichSuPhieu)) {
    return normalized as TrangThaiLichSuPhieu;
  }

  return null;
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof NguoiDungApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the tai du lieu lich su phieu cuu tro.";
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

function getStatusLabel(status: TrangThaiLichSuPhieu): string {
  switch (status) {
    case "DA_NHAN":
      return "Đã nhận nhiệm vụ";
    case "DANG_TREN_DUONG_TOI":
      return "Đang trên đường tới";
    case "DANG_XU_LY":
      return "Đang xử lý";
    case "HOAN_THANH":
      return "Hoàn thành";
    case "HUY":
      return "Đã huỷ";
    default:
      return "Chưa cập nhật";
  }
}

function getStatusBadgeClassName(status: TrangThaiLichSuPhieu): string {
  switch (status) {
    case "HOAN_THANH":
      return "border-success-300 bg-success-50 text-success-700 dark:border-success-500/30 dark:bg-success-500/15 dark:text-success-400";
    case "HUY":
      return "border-danger-300 bg-danger-50 text-danger-700 dark:border-danger-500/30 dark:bg-danger-500/15 dark:text-danger-300";
    default:
      return "border-brand-200 bg-brand-50 text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300";
  }
}

function ProcessStepIcon() {
  return (
    <span className="inline-flex h-8 w-8 shrink-0 items-center justify-center text-indigo-600">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        className="h-7 w-7 animate-spin"
        aria-hidden="true"
      >
        <path
          d="M20 12a8 8 0 0 0-13.66-5.66"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
        />
        <path
          d="M6 2v4h4"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M4 12a8 8 0 0 0 13.66 5.66"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
        />
        <path
          d="M18 22v-4h-4"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    </span>
  );
}

function PendingStepIcon() {
  return (
    <span className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full border-2 border-indigo-500 bg-white">
      <span className="h-3.5 w-3.5 rounded-full bg-indigo-500" />
    </span>
  );
}

function DoneStepIcon() {
  return (
    <span className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-indigo-500 text-white">
      <svg
        viewBox="0 0 20 20"
        fill="none"
        className="h-4 w-4"
        aria-hidden="true"
      >
        <path
          d="M5 10.5L8.3 13.7L15 7"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    </span>
  );
}

function buildLichSuCuuTroItems(phieuList: PhieuCuuTroDto[]): LichSuCuuTroItem[] {
  return phieuList
    .map((phieu) => {
      const currentStatus = toLichSuTrangThai(phieu.trangThai);
      if (!currentStatus) {
        return null;
      }

      return {
        id: phieu.id,
        loaiCuuTroTen: trimOrFallback(phieu.loaiSuCo?.ten, "Loại cứu trợ chưa cập nhật"),
        loaiCuuTroIconUrl: trimOrFallback(phieu.loaiSuCo?.iconUrl, DEFAULT_CUU_TRO_ICON),
        createdAt: phieu.createdAt,
        hoTenNguoiCanHoTro: trimOrFallback(phieu.nguoiGui?.ten, "Chưa cập nhật"),
        soDienThoaiNguoiCanHoTro: trimOrFallback(phieu.nguoiGui?.sdt, "Chưa cập nhật"),
        diaChiNguoiCanHoTro: trimOrFallback(phieu.viTri?.diaChi, "Chưa cập nhật"),
        ghiChu: trimOrFallback(phieu.ghiChu, "Không có ghi chú"),
        currentStatus,
      };
    })
    .filter((item): item is LichSuCuuTroItem => item !== null)
    .sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
    );
}

export default function LichSuCuuTroPage() {
  const [phieuApiList, setPhieuApiList] = useState<PhieuCuuTroDto[]>([]);
  const [isDataLoading, setIsDataLoading] = useState<boolean>(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [expandedById, setExpandedById] = useState<Record<number, boolean>>({});

  const loadPageData = useCallback(async (): Promise<void> => {
    setIsDataLoading(true);
    setApiError(null);

    try {
      const phieuResult = await fetchPhieuCuuTroList();
      setPhieuApiList(phieuResult);
    } catch (error) {
      setApiError(getApiErrorMessage(error));
      setPhieuApiList([]);
    } finally {
      setIsDataLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadPageData();
  }, [loadPageData]);

  const phieuItems = useMemo(
    () => buildLichSuCuuTroItems(phieuApiList),
    [phieuApiList]
  );

  const toggleExpanded = (id: number) => {
    setExpandedById((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  return (
    <>
      <PageMeta
        title="Lịch sử phiếu cứu trợ"
        description="Danh sách lịch sử các phiếu cứu trợ và trạng thái xử lý."
      />
      <PageBreadCrumb pageTitle="Lịch sử phiếu cứu trợ" />

      <div className="space-y-6">
        <ComponentCard
          title="Danh sách lịch sử"
          desc=""
        >
          <div className="space-y-4">
            {isDataLoading && (
              <div className="rounded-lg border border-dashed border-gray-300 p-5 text-center text-theme-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
                Đang tải dữ liệu lịch sử phiếu cứu trợ...
              </div>
            )}

            {!isDataLoading && apiError && (
              <div className="rounded-lg border border-danger-200 bg-danger-50 p-5 text-center text-theme-sm text-danger-700 dark:border-danger-500/20 dark:bg-danger-500/10 dark:text-danger-400">
                {apiError}
              </div>
            )}

            {!isDataLoading && !apiError && phieuItems.map((phieuItem, index) => {
              const isExpanded = expandedById[phieuItem.id] ?? index === 0;
              const currentStepIndex = HISTORY_PROGRESS_STEPS.findIndex(
                (step) => step.key === phieuItem.currentStatus
              );
              const isCancelled = phieuItem.currentStatus === "HUY";

              const totalSegments = Math.max(0, HISTORY_PROGRESS_STEPS.length - 1);
              const doneSegmentCount =
                currentStepIndex < 0
                  ? 0
                  : Math.max(0, Math.min(currentStepIndex, totalSegments));
              const doneLineWidthPercent =
                totalSegments > 0 ? (doneSegmentCount / totalSegments) * 100 : 0;
              const trackSidePercent = 100 / (HISTORY_PROGRESS_STEPS.length * 2);
              const lineUsablePercent = 100 - trackSidePercent * 2;

              return (
                <article
                  key={phieuItem.id}
                  className="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-white/[0.08] dark:bg-white/[0.03]"
                >
                  <div className="flex flex-wrap items-center gap-3 p-4 sm:p-5">
                    <div className="h-11 w-11 shrink-0 overflow-hidden rounded-full border border-gray-200 bg-gray-50 p-2 dark:border-white/[0.08] dark:bg-white/[0.04]">
                      <img
                        src={phieuItem.loaiCuuTroIconUrl}
                        alt={phieuItem.loaiCuuTroTen}
                        className="h-full w-full object-contain"
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

                    <div className="ml-auto flex items-center gap-3">
                      <span
                        className={`inline-flex rounded-xl border px-4 py-1.5 text-sm font-medium ${getStatusBadgeClassName(
                          phieuItem.currentStatus
                        )}`}
                      >
                        {getStatusLabel(phieuItem.currentStatus)}
                      </span>
                      <button
                        type="button"
                        onClick={() => toggleExpanded(phieuItem.id)}
                        className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-gray-300 bg-white text-gray-700 transition-colors hover:bg-gray-50 dark:border-white/[0.12] dark:bg-transparent dark:text-gray-200 dark:hover:bg-white/[0.05]"
                        aria-label={
                          isExpanded ? "Thu gọn chi tiết phiếu" : "Mở chi tiết phiếu"
                        }
                      >
                        <ChevronDownIcon
                          className={`size-5 transition-transform duration-200 ${
                            isExpanded ? "rotate-180" : "rotate-0"
                          }`}
                        />
                      </button>
                    </div>
                  </div>

                  <div
                    className={`grid transition-all duration-300 ease-out ${
                      isExpanded
                        ? "grid-rows-[1fr] opacity-100"
                        : "pointer-events-none grid-rows-[0fr] opacity-0"
                    }`}
                  >
                    <div className="overflow-hidden border-t border-gray-200 bg-gray-50/80 p-4 sm:p-5 dark:border-white/[0.08] dark:bg-white/[0.02]">
                      {isCancelled ? (
                        <div className="rounded-xl border border-danger-200 bg-danger-50 px-4 py-3 text-sm text-danger-700 dark:border-danger-500/30 dark:bg-danger-500/10 dark:text-danger-300">
                          Phiếu này đã bị huỷ. Đội không tiếp tục xử lý nhiệm vụ.
                        </div>
                      ) : (
                        <div className="mx-auto w-full max-w-3xl">
                          <div className="relative grid grid-cols-4 items-center">
                            <span
                              className="pointer-events-none absolute top-1/2 h-[3px] -translate-y-1/2 rounded-full bg-gray-200 dark:bg-white/[0.12]"
                              style={{
                                left: `${trackSidePercent}%`,
                                right: `${trackSidePercent}%`,
                              }}
                            />
                            <span
                              className="pointer-events-none absolute top-1/2 h-[3px] -translate-y-1/2 rounded-full bg-indigo-500"
                              style={{
                                left: `${trackSidePercent}%`,
                                width: `${(lineUsablePercent * doneLineWidthPercent) / 100}%`,
                              }}
                            />

                            {HISTORY_PROGRESS_STEPS.map((step, stepIndex) => (
                              <div
                                key={step.key}
                                className="relative z-10 flex items-center justify-center"
                              >
                                {(() => {
                                  const isDone =
                                    stepIndex < currentStepIndex ||
                                    (phieuItem.currentStatus === "HOAN_THANH" &&
                                      stepIndex === currentStepIndex);
                                  const isCurrent = stepIndex === currentStepIndex;

                                  if (isDone) {
                                    return <DoneStepIcon />;
                                  }

                                  if (isCurrent) {
                                    return <ProcessStepIcon />;
                                  }

                                  return <PendingStepIcon />;
                                })()}
                              </div>
                            ))}
                          </div>

                          <div className="mt-3 grid grid-cols-4 gap-2">
                            {HISTORY_PROGRESS_STEPS.map((step, stepIndex) => (
                              <p
                                key={`${phieuItem.id}-${step.key}`}
                                className={`text-center text-sm ${
                                  stepIndex === currentStepIndex
                                    ? "font-semibold text-gray-800 dark:text-white/90"
                                    : "text-gray-600 dark:text-gray-300"
                                }`}
                              >
                                {step.label}
                              </p>
                            ))}
                          </div>
                        </div>
                      )}

                      <div className="mt-6 grid gap-4 md:grid-cols-2">
                        <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-white/[0.12] dark:bg-white">
                          <p className="text-theme-xs text-gray-500">Người cần hỗ trợ</p>
                          <p className="mt-1 text-sm font-semibold text-gray-800">
                            {phieuItem.hoTenNguoiCanHoTro}
                          </p>
                          <p className="mt-1 text-theme-xs text-gray-600">
                            SĐT: {phieuItem.soDienThoaiNguoiCanHoTro}
                          </p>
                          <p className="mt-1 text-theme-xs text-gray-600">
                            Địa chỉ: {phieuItem.diaChiNguoiCanHoTro}
                          </p>
                        </div>

                        <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-white/[0.12] dark:bg-white">
                          <p className="text-theme-xs text-gray-500">Ghi chú phiếu</p>
                          <p className="mt-1 text-sm font-semibold text-gray-800">
                            {phieuItem.ghiChu}
                          </p>
                          <p className="mt-1 text-theme-xs text-gray-600">
                            Trạng thái hiện tại: {getStatusLabel(phieuItem.currentStatus)}
                          </p>
                          <p className="mt-1 text-theme-xs text-gray-600">
                            Thời gian tạo: {formatDateTime(phieuItem.createdAt)}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </article>
              );
            })}

            {!isDataLoading && !apiError && phieuItems.length === 0 && (
              <div className="rounded-xl border border-dashed border-gray-300 p-5 text-center text-theme-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
                Chưa có dữ liệu lịch sử phiếu cứu trợ.
              </div>
            )}
          </div>
        </ComponentCard>
      </div>
    </>
  );
}
