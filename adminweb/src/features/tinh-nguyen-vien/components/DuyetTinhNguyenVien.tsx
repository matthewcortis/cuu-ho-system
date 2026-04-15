import { useEffect, useState } from "react";
import { CheckLineIcon, TrashBinIcon } from "@/icons";
import {
  duyetTinhNguyenVien,
  fetchChoXetDuyetTinhNguyenVien,
  TinhNguyenVienApiError,
  type TinhNguyenVienChoDuyetItem,
  xoaTinhNguyenVien,
} from "@/features/tinh-nguyen-vien/api/tinhNguyenVienApi";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

function getApiErrorMessage(error: unknown): string {
  if (error instanceof TinhNguyenVienApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Không thể xử lý yêu cầu tình nguyện viên. Vui lòng thử lại.";
}

function extractSimpleDate(raw: string): string {
  const parsed = Date.parse(raw);
  if (Number.isNaN(parsed)) {
    return "Không rõ thời gian";
  }

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(parsed);
}

export default function DuyetTinhNguyenVien() {
  const [volunteers, setVolunteers] = useState<TinhNguyenVienChoDuyetItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isMutatingByVolunteerId, setIsMutatingByVolunteerId] = useState<
    Record<number, boolean>
  >({});

  useEffect(() => {
    let isMounted = true;

    const loadVolunteers = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const pendingVolunteers = await fetchChoXetDuyetTinhNguyenVien();
        if (!isMounted) {
          return;
        }
        setVolunteers(pendingVolunteers);
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

    void loadVolunteers();

    return () => {
      isMounted = false;
    };
  }, []);

  const setMutatingState = (volunteerId: number, isMutating: boolean) => {
    setIsMutatingByVolunteerId((prev) => {
      if (isMutating) {
        return {
          ...prev,
          [volunteerId]: true,
        };
      }

      const nextState = { ...prev };
      delete nextState[volunteerId];
      return nextState;
    });
  };

  const handleApprove = async (volunteer: TinhNguyenVienChoDuyetItem) => {
    if (isMutatingByVolunteerId[volunteer.id]) {
      return;
    }

    setActionError(null);
    setMutatingState(volunteer.id, true);

    try {
      await duyetTinhNguyenVien(volunteer.id);
      setVolunteers((prev) => prev.filter((item) => item.id !== volunteer.id));
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setMutatingState(volunteer.id, false);
    }
  };

  const handleDelete = async (volunteer: TinhNguyenVienChoDuyetItem) => {
    if (isMutatingByVolunteerId[volunteer.id]) {
      return;
    }

    const shouldDelete = window.confirm(
      `Bạn có chắc chắn muốn xóa tình nguyện viên ${volunteer.ten}?`
    );
    if (!shouldDelete) {
      return;
    }

    setActionError(null);
    setMutatingState(volunteer.id, true);

    try {
      await xoaTinhNguyenVien(volunteer.id);
      setVolunteers((prev) => prev.filter((item) => item.id !== volunteer.id));
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setMutatingState(volunteer.id, false);
    }
  };

  return (
    <>
      {loadError && (
        <div className="mb-4 rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
          Không thể lấy danh sách tình nguyện viên chờ duyệt: {loadError}
        </div>
      )}

      {actionError && (
        <div className="mb-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-theme-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-300">
          {actionError}
        </div>
      )}

      {isLoading && (
        <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
          Đang tải danh sách tình nguyện viên chờ duyệt...
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
        <div className="max-w-full overflow-x-auto custom-scrollbar">
          <Table className="min-w-[1120px]">
            <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
              <TableRow>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Thông tin
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
                  Có thể giúp
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Ghi chú
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
              {volunteers.map((volunteer) => {
                const isMutating = Boolean(isMutatingByVolunteerId[volunteer.id]);

                return (
                  <TableRow key={volunteer.id}>
                    <TableCell className="px-5 py-4 sm:px-6 text-start">
                      <div className="flex items-start gap-3">
                        <div className="h-12 w-12 overflow-hidden rounded-full">
                          <img
                            width={48}
                            height={48}
                            src={volunteer.avatarUrl}
                            alt={volunteer.ten}
                            className="size-12 object-cover"
                          />
                        </div>
                        <div className="space-y-1">
                          <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                            {volunteer.ten}
                          </span>
                          <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                            SĐT: {volunteer.soDienThoai}
                          </span>
                          <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                            Email: {volunteer.email}
                          </span>
                          <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                            Đăng ký lúc: {extractSimpleDate(volunteer.createdAt)}
                          </span>
                        </div>
                      </div>
                    </TableCell>

                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {volunteer.diaChi}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {volunteer.kyNang}
                    </TableCell>
                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {volunteer.ghiChu}
                    </TableCell>

                    <TableCell className="px-4 py-3">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleApprove(volunteer)}
                          className="inline-flex items-center gap-1 rounded-lg bg-brand-500 px-3 py-2 text-theme-xs font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:bg-brand-300"
                          aria-label={`Chấp nhận ${volunteer.ten}`}
                          disabled={isMutating}
                        >
                          <CheckLineIcon className="size-4" />
                          {isMutating ? "Đang xử lý..." : "Chấp nhận"}
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDelete(volunteer)}
                          className="inline-flex items-center gap-1 rounded-lg border border-error-200 px-3 py-2 text-theme-xs font-medium text-error-600 hover:bg-error-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                          aria-label={`Xóa ${volunteer.ten}`}
                          disabled={isMutating}
                        >
                          <TrashBinIcon className="size-4" />
                          Xóa
                        </button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })}

              {!isLoading && volunteers.length === 0 && (
                <TableRow>
                  <td
                    className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                    colSpan={5}
                  >
                    Không có tình nguyện viên nào đang chờ duyệt.
                  </td>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </div>
    </>
  );
}