import { useState } from "react";
import { EyeCloseIcon, EyeIcon, PencilIcon, TrashBinIcon } from "@/icons";
import { Modal } from "@/components/ui/modal";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export interface ThemVatPhamItem {
  id: number;
  tenVatPham: string;
  soLuong: number;
  donVi: string;
  donViId: number | null;
  nhomVatPham: string;
  nhomVatPhamId: number | null;
  imageUrl: string;
  imagePath: string;
  tepTinId: number | null;
  trangThai: boolean;
  createdAt: string;
}

interface ThemVatPhamTableProps {
  items: ThemVatPhamItem[];
  onEditItem?: (item: ThemVatPhamItem) => void;
  onToggleVisibilityItem?: (item: ThemVatPhamItem) => void;
  onDeleteItem?: (item: ThemVatPhamItem) => void;
}

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Chưa cập nhật";
  return date.toLocaleDateString("vi-VN");
}

export default function ThemVatPhamTable({
  items,
  onEditItem,
  onToggleVisibilityItem,
  onDeleteItem,
}: ThemVatPhamTableProps) {
  const [previewImage, setPreviewImage] = useState<{
    url: string;
    alt: string;
  } | null>(null);

  return (
    <>
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
        <div className="max-w-full overflow-x-auto custom-scrollbar">
          <Table className="min-w-[1080px]">
            <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
              <TableRow>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Ảnh
                </TableCell>
                <TableCell
                  isHeader
                  className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Tên vật phẩm
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Nhóm vật phẩm
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Số lượng
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Đơn vị
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
              {items.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="px-5 py-4 text-start">
                    {item.imageUrl ? (
                      <button
                        type="button"
                        onClick={() =>
                          setPreviewImage({
                            url: item.imageUrl,
                            alt: item.tenVatPham,
                          })
                        }
                        className="w-12 h-12 overflow-hidden rounded-lg border border-gray-200 dark:border-gray-700"
                      >
                        <img
                          src={item.imageUrl}
                          alt={item.tenVatPham}
                          className="h-full w-full object-cover"
                        />
                      </button>
                    ) : (
                      <span className="text-theme-xs text-gray-500 dark:text-gray-400">
                        Chưa có ảnh
                      </span>
                    )}
                  </TableCell>
                  <TableCell className="px-5 py-4 text-start">
                    <p className="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                      {item.tenVatPham}
                    </p>
                  </TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {item.nhomVatPham}
                  </TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {item.soLuong}
                  </TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {item.donVi}
                  </TableCell>
                  <TableCell className="px-4 py-3 text-start">
                    <span
                      className={`inline-flex rounded-full px-2.5 py-1 text-theme-xs font-medium ${
                        item.trangThai
                          ? "bg-success-50 text-success-700 dark:bg-success-500/10 dark:text-success-400"
                          : "bg-warning-50 text-warning-700 dark:bg-warning-500/10 dark:text-warning-300"
                      }`}
                    >
                      {item.trangThai ? "Hiện" : "Đã ẩn"}
                    </span>
                  </TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {formatDate(item.createdAt)}
                  </TableCell>
                  <TableCell className="px-4 py-3">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        type="button"
                        onClick={() => onEditItem?.(item)}
                        className="inline-flex items-center justify-center w-8 h-8 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-100 hover:text-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                        aria-label={`Sửa ${item.tenVatPham}`}
                      >
                        <PencilIcon className="size-4" />
                      </button>
                      <button
                        type="button"
                        onClick={() => onToggleVisibilityItem?.(item)}
                        className={`inline-flex items-center justify-center w-8 h-8 rounded-lg border ${
                          item.trangThai
                            ? "text-success-600 border-success-200 hover:bg-success-50 dark:border-success-500/30 dark:text-success-400 dark:hover:bg-success-500/10"
                            : "text-warning-600 border-warning-200 hover:bg-warning-50 dark:border-warning-500/30 dark:text-warning-300 dark:hover:bg-warning-500/10"
                        }`}
                        aria-label={
                          item.trangThai
                            ? `Ẩn ${item.tenVatPham}`
                            : `Hiện ${item.tenVatPham}`
                        }
                      >
                        {item.trangThai ? (
                          <EyeIcon className="fill-gray-500 dark:fill-gray-400 size-4" />
                        ) : (
                          <EyeCloseIcon className="fill-gray-500 dark:fill-gray-400 size-4" />
                        )}
                      </button>
                      <button
                        type="button"
                        onClick={() => onDeleteItem?.(item)}
                        className="inline-flex items-center justify-center w-8 h-8 text-error-600 border border-error-200 rounded-lg hover:bg-error-50 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                        aria-label={`Xóa ${item.tenVatPham}`}
                      >
                        <TrashBinIcon className="size-4" />
                      </button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}

              {items.length === 0 && (
                <TableRow>
                  <td
                    className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                    colSpan={8}
                  >
                    Chưa có vật phẩm nào.
                  </td>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      <Modal
        isOpen={Boolean(previewImage)}
        onClose={() => setPreviewImage(null)}
        className="max-w-[860px] m-4 p-4 sm:p-6"
      >
        {previewImage ? (
          <div className="space-y-4">
            <p className="text-sm font-medium text-gray-700 dark:text-gray-200">
              {previewImage.alt}
            </p>
            <div className="max-h-[70vh] overflow-auto rounded-lg border border-gray-200 dark:border-gray-700">
              <img
                src={previewImage.url}
                alt={previewImage.alt}
                className="w-full h-auto object-contain"
              />
            </div>
          </div>
        ) : null}
      </Modal>
    </>
  );
}