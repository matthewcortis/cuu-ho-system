import { useCallback, useEffect, useMemo, useState } from "react";
import ActionToast, { type ActionToastData } from "@/components/common/ActionToast";
import ComponentCard from "@/components/common/ComponentCard";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import PageMeta from "@/components/common/PageMeta";
import Pagination from "@/components/common/Pagination";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import Badge from "@/components/ui/badge/Badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { TrashBinIcon } from "@/icons";
import {
  BangTinApiError,
  deleteBangTin,
  fetchBangTinAdminList,
  type BangTinDto,
} from "@/features/bang-tin/api/bangTinApi";

interface BangTinTableItem {
  id: number;
  tieuDe: string;
  noiDung: string;
  nguoiDangTen: string;
  nguoiDangSdt: string;
  mediaUrl: string;
  mediaType: string;
  trangThai: boolean;
  createdAt: string;
}

const ROWS_PER_PAGE_OPTIONS = [10, 20, 50] as const;

function mapBangTinDtoToItem(item: BangTinDto): BangTinTableItem {
  return {
    id: item.id,
    tieuDe: item.tieuDe.trim() || "Chua dat tieu de",
    noiDung: item.noiDung.trim() || "Khong co noi dung",
    nguoiDangTen: item.nguoiDung?.ten?.trim() || "Khong ro nguoi dang",
    nguoiDangSdt: item.nguoiDung?.sdt?.trim() || "Chua cap nhat",
    mediaUrl: item.tepTin?.duongDan?.trim() || "",
    mediaType: item.tepTin?.loaiTepTin?.trim().toLowerCase() || "",
    trangThai: item.trangThai,
    createdAt: item.createdAt,
  };
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof BangTinApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the xu ly du lieu bang tin.";
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Chua cap nhat";
  }

  return date.toLocaleString("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

function isVideo(mediaType: string): boolean {
  return mediaType.includes("video");
}

export default function QuanLyBangTinPage() {
  const [items, setItems] = useState<BangTinTableItem[]>([]);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [rowsPerPage, setRowsPerPage] = useState<number>(ROWS_PER_PAGE_OPTIONS[0]);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [isLoading, setIsLoading] = useState(false);
  const [isDeletingId, setIsDeletingId] = useState<number | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionToast, setActionToast] = useState<ActionToastData | null>(null);

  const showToast = useCallback((type: "success" | "error", title: string, message: string) => {
    setActionToast({
      id: Date.now(),
      type,
      title,
      message,
    });
  }, []);

  useEffect(() => {
    let isMounted = true;

    const loadBangTin = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const remoteItems = await fetchBangTinAdminList();
        if (!isMounted) {
          return;
        }
        setItems(remoteItems.map(mapBangTinDtoToItem));
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

    void loadBangTin();

    return () => {
      isMounted = false;
    };
  }, []);

  const filteredItems = useMemo(() => {
    const normalizedKeyword = searchKeyword.trim().toLowerCase();
    if (!normalizedKeyword) {
      return items;
    }

    return items.filter((item) => {
      return (
        item.tieuDe.toLowerCase().includes(normalizedKeyword) ||
        item.noiDung.toLowerCase().includes(normalizedKeyword) ||
        item.nguoiDangTen.toLowerCase().includes(normalizedKeyword)
      );
    });
  }, [items, searchKeyword]);

  const totalFilteredItems = filteredItems.length;
  const totalPages = Math.max(1, Math.ceil(totalFilteredItems / rowsPerPage));
  const paginatedItems = useMemo(() => {
    const startIndex = (currentPage - 1) * rowsPerPage;
    return filteredItems.slice(startIndex, startIndex + rowsPerPage);
  }, [currentPage, filteredItems, rowsPerPage]);

  useEffect(() => {
    setCurrentPage(1);
  }, [rowsPerPage, searchKeyword]);

  useEffect(() => {
    setCurrentPage((prev) => Math.min(prev, totalPages));
  }, [totalPages]);

  const handleDeleteItem = async (item: BangTinTableItem) => {
    if (isDeletingId !== null) {
      return;
    }

    const confirmed = window.confirm(
      `Ban co chac chan muon xoa bai viet "${item.tieuDe}" (ID: ${item.id})?`
    );
    if (!confirmed) {
      return;
    }

    setIsDeletingId(item.id);
    try {
      await deleteBangTin(item.id);
      setItems((prev) => prev.filter((currentItem) => currentItem.id !== item.id));
      showToast("success", "Xoa bai viet thanh cong", `Da xoa bai viet "${item.tieuDe}".`);
    } catch (error) {
      showToast("error", "Khong the xoa bai viet", getApiErrorMessage(error));
    } finally {
      setIsDeletingId(null);
    }
  };

  return (
    <>
      <PageMeta title="Quản lý bảng tin" description="Trang quản lý toàn bộ bản tin người dùng." />
      <PageBreadcrumb pageTitle="Quản lý bảng tin" />

      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Không thể lấy danh sách bảng tin từ backend: {loadError}
          </div>
        )}

        <ComponentCard
          title="Danh sách bản tin"
          desc="Hiển thị bài viết của tất cả người dùng và hỗ trợ xóa bài."
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Đang tải danh sách bảng tin...
            </div>
          )}

          <div className="space-y-5">
            <div className="grid grid-cols-1 gap-4 lg:grid-cols-4">
              <div className="lg:col-span-3">
                <Label htmlFor="tim-kiem-bang-tin">Tìm kiếm bản tin</Label>
                <Input
                  id="tim-kiem-bang-tin"
                  type="text"
                  placeholder="Nhập tiêu đề, nội dung hoặc tên người đăng"
                  value={searchKeyword}
                  onChange={(event) => setSearchKeyword(event.target.value)}
                />
              </div>

              <div>
                <Label htmlFor="bang-tin-rows-per-page">Số dòng/trang</Label>
                <select
                  id="bang-tin-rows-per-page"
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
              Hiển thị {paginatedItems.length}/{totalFilteredItems} bản tin (tổng: {items.length})
            </p>

            <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
              <div className="max-w-full overflow-x-auto custom-scrollbar">
                <Table className="min-w-[1240px]">
                  <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                    <TableRow>
                      <TableCell
                        isHeader
                        className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                      >
                        ID
                      </TableCell>
                      <TableCell
                        isHeader
                        className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                      >
                        Tiêu đề
                      </TableCell>
                      <TableCell
                        isHeader
                        className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                      >
                        Nội dung
                      </TableCell>
                      <TableCell
                        isHeader
                        className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                      >
                        Người đăng
                      </TableCell>
                      <TableCell
                        isHeader
                        className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                      >
                        Media
                      </TableCell>
                      <TableCell
                        isHeader
                        className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                      >
                        Trạng thái
                      </TableCell>
                      <TableCell
                        isHeader
                        className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                      >
                        Ngày tạo
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
                    {paginatedItems.map((item) => {
                      const deleting = isDeletingId === item.id;

                      return (
                        <TableRow key={item.id}>
                          <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                            #{item.id}
                          </TableCell>

                          <TableCell className="px-5 py-4 text-start">
                            <p className="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                              {item.tieuDe}
                            </p>
                          </TableCell>

                          <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                            <p className="max-w-[380px] truncate">{item.noiDung}</p>
                          </TableCell>

                          <TableCell className="px-4 py-3 text-start text-theme-sm">
                            <p className="font-medium text-gray-800 dark:text-white/90">
                              {item.nguoiDangTen}
                            </p>
                            <p className="text-gray-500 dark:text-gray-400">{item.nguoiDangSdt}</p>
                          </TableCell>

                          <TableCell className="px-4 py-3 text-start text-theme-sm">
                            {item.mediaUrl ? (
                              <div className="flex items-center gap-2">
                                {!isVideo(item.mediaType) && (
                                  <img
                                    src={item.mediaUrl}
                                    alt={item.tieuDe}
                                    className="h-10 w-10 rounded-lg border border-gray-200 object-cover dark:border-gray-700"
                                    onError={(event) => {
                                      event.currentTarget.style.display = "none";
                                    }}
                                  />
                                )}
                                <a
                                  href={item.mediaUrl}
                                  target="_blank"
                                  rel="noreferrer"
                                  className="text-brand-600 hover:underline dark:text-brand-400"
                                >
                                  {isVideo(item.mediaType) ? "Xem video" : "Xem ảnh"}
                                </a>
                              </div>
                            ) : (
                              <span className="text-gray-500 dark:text-gray-400">Không có</span>
                            )}
                          </TableCell>

                          <TableCell className="px-4 py-3 text-start">
                            <Badge size="sm" color={item.trangThai ? "success" : "warning"}>
                              {item.trangThai ? "Công khai" : "Ẩn"}
                            </Badge>
                          </TableCell>

                          <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                            {formatDateTime(item.createdAt)}
                          </TableCell>

                          <TableCell className="px-4 py-3">
                            <div className="flex items-center justify-center">
                              <button
                                type="button"
                                onClick={() => {
                                  void handleDeleteItem(item);
                                }}
                                disabled={isDeletingId !== null}
                                className="inline-flex items-center justify-center w-8 h-8 text-error-600 border border-error-200 rounded-lg hover:bg-error-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                                aria-label={`Xoa bai viet ${item.tieuDe}`}
                              >
                                {deleting ? (
                                  <span className="text-[10px] font-medium">...</span>
                                ) : (
                                  <TrashBinIcon className="size-4" />
                                )}
                              </button>
                            </div>
                          </TableCell>
                        </TableRow>
                      );
                    })}

                    {paginatedItems.length === 0 && (
                      <TableRow>
                        <td
                          className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                          colSpan={8}
                        >
                          {totalFilteredItems === 0
                            ? "Không có bản tin phù hợp với bộ lọc."
                            : "Chưa có bản tin nào."}
                        </td>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            </div>

            <Pagination
              currentPage={currentPage}
              totalItems={totalFilteredItems}
              itemsPerPage={rowsPerPage}
              totalItemsOverall={items.length}
              onPageChange={setCurrentPage}
            />
          </div>
        </ComponentCard>
      </div>

      <ActionToast toast={actionToast} onClose={() => setActionToast(null)} />
    </>
  );
}
