import { useMemo, useState } from "react";
import ComponentCard from "@/components/common/ComponentCard";
import PageBreadCrumb from "@/components/common/PageBreadCrumb";
import PageMeta from "@/components/common/PageMeta";
import { mockDatabase } from "@/data";
import { ChevronDownIcon } from "@/icons";

type HistoryStepKey = "pending" | "processing" | "completed";

type LichSuCuuTroItem = {
  id: number;
  loaiCuuTroTen: string;
  loaiCuuTroIconUrl: string;
  createdAt: string;
  hoTenNguoiCanHoTro: string;
  soDienThoaiNguoiCanHoTro: string;
  diaChiNguoiCanHoTro: string;
  tenDoiCuuHo: string;
  soDienThoaiDoiCuuHo: string;
  diaChiDoiCuuHo: string;
  currentStep: HistoryStepKey;
};

const DEFAULT_CUU_TRO_ICON = "/images/icons/file-image.svg";
const HISTORY_STEPS: Array<{ key: HistoryStepKey; label: string }> = [
  { key: "pending", label: "Chờ xác nhận" },
  { key: "processing", label: "Đang xử lý" },
  { key: "completed", label: "Hoàn thành" },
];

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

function getStepLabel(step: HistoryStepKey): string {
  return HISTORY_STEPS.find((item) => item.key === step)?.label ?? "Chờ xác nhận";
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

function buildLichSuCuuTroItems(): LichSuCuuTroItem[] {
  const fallbackTeam = mockDatabase.doi_nhom[0];
  const stepCycle: HistoryStepKey[] = ["pending", "processing", "completed"];

  return mockDatabase.phieu_cuu_tro
    .map((phieu, index) => {
      const danhSachCuuTro = mockDatabase.danh_sach_cuu_tro.find(
        (item) => item.id === phieu.danh_sach_cuu_tro_id
      );
      const viTriNguoiCanHoTro = mockDatabase.vi_tri.find(
        (item) => item.id === phieu.vi_tri_id
      );
      const doiCuuHo =
        mockDatabase.doi_nhom[index % mockDatabase.doi_nhom.length] ?? fallbackTeam;
      const viTriDoiCuuHo = mockDatabase.vi_tri.find(
        (item) => item.id === doiCuuHo?.vi_tri_id
      );

      return {
        id: phieu.id,
        loaiCuuTroTen: danhSachCuuTro?.ten ?? "Loại cứu trợ chưa cập nhật",
        loaiCuuTroIconUrl: danhSachCuuTro?.icon_url ?? DEFAULT_CUU_TRO_ICON,
        createdAt: phieu.created_at,
        hoTenNguoiCanHoTro: phieu.ho_ten ?? "Chưa cập nhật",
        soDienThoaiNguoiCanHoTro: phieu.sdt ?? "Chưa cập nhật",
        diaChiNguoiCanHoTro: viTriNguoiCanHoTro?.dia_chi ?? "Chưa cập nhật",
        tenDoiCuuHo: doiCuuHo?.ten_doi_nhom ?? "Chưa phân đội",
        soDienThoaiDoiCuuHo: doiCuuHo?.so_dien_thoai ?? "Chưa cập nhật",
        diaChiDoiCuuHo: viTriDoiCuuHo?.dia_chi ?? "Chưa cập nhật",
        currentStep: stepCycle[index % stepCycle.length],
      };
    })
    .sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
    );
}

export default function LichSuCuuTroPage() {
  const phieuItems = useMemo(() => buildLichSuCuuTroItems(), []);
  const [expandedById, setExpandedById] = useState<Record<number, boolean>>({});

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
            {phieuItems.map((phieuItem, index) => {
              const isExpanded = expandedById[phieuItem.id] ?? index === 0;
              const currentStepIndex = HISTORY_STEPS.findIndex(
                (step) => step.key === phieuItem.currentStep
              );
              const isCompletedFlow = phieuItem.currentStep === "completed";
              const totalSegments = Math.max(0, HISTORY_STEPS.length - 1);
              const doneSegmentCount = Math.max(
                0,
                Math.min(currentStepIndex, totalSegments)
              );
              const doneLineWidthPercent =
                totalSegments > 0 ? (doneSegmentCount / totalSegments) * 100 : 0;

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
                      <span className="inline-flex rounded-xl border border-gray-300 bg-white px-4 py-1.5 text-sm font-medium text-gray-700 dark:border-white/[0.12] dark:bg-white/[0.03] dark:text-gray-200">
                        {getStepLabel(phieuItem.currentStep)}
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
                      <div className="mx-auto w-full max-w-2xl">
                        <div className="relative grid grid-cols-3 items-center">
                          <span className="pointer-events-none absolute left-[16.6667%] right-[16.6667%] top-1/2 h-[3px] -translate-y-1/2 rounded-full bg-gray-200 dark:bg-white/[0.12]" />
                          <span
                            className="pointer-events-none absolute left-[16.6667%] top-1/2 h-[3px] -translate-y-1/2 rounded-full bg-indigo-500"
                            style={{
                              width: `calc((100% - 33.3334%) * ${doneLineWidthPercent / 100})`,
                            }}
                          />

                          {HISTORY_STEPS.map((step, stepIndex) => (
                            <div
                              key={step.key}
                              className="relative z-10 flex items-center justify-center"
                            >
                              {(() => {
                                const isDone =
                                  stepIndex < currentStepIndex ||
                                  (isCompletedFlow && stepIndex === currentStepIndex);
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

                        <div className="mt-3 grid grid-cols-3 gap-2">
                          {HISTORY_STEPS.map((step, stepIndex) => (
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

                      <div className="mt-6 grid gap-4 md:grid-cols-2">
                        <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-white/[0.12] dark:bg-white">
                          <p className="text-theme-xs text-gray-500">Đội cứu hộ đã chọn</p>
                          <p className="mt-1 text-sm font-semibold text-gray-800">
                            {phieuItem.tenDoiCuuHo}
                          </p>
                          <p className="mt-1 text-theme-xs text-gray-600">
                            SĐT: {phieuItem.soDienThoaiDoiCuuHo}
                          </p>
                          <p className="mt-1 text-theme-xs text-gray-600">
                            Địa chỉ: {phieuItem.diaChiDoiCuuHo}
                          </p>
                        </div>

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
                      </div>
                    </div>
                  </div>
                </article>
              );
            })}

            {phieuItems.length === 0 && (
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

