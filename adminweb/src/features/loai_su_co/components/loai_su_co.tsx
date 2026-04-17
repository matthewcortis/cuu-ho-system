import { PencilIcon, TrashBinIcon } from "@/icons";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export interface LoaiSuCoItem {
  id: number;
  ten: string;
  iconUrl: string;
  createdAt: string;
}

interface LoaiSuCoTableProps {
  items: LoaiSuCoItem[];
  onEditItem?: (item: LoaiSuCoItem) => void;
  onDeleteItem?: (item: LoaiSuCoItem) => void;
}

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Chua cap nhat";
  }
  return date.toLocaleDateString("vi-VN");
}

export default function LoaiSuCoTable({
  items,
  onEditItem,
  onDeleteItem,
}: LoaiSuCoTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="max-w-full overflow-x-auto custom-scrollbar">
        <Table className="min-w-[880px]">
          <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell
                isHeader
                className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Icon
              </TableCell>
              <TableCell
                isHeader
                className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Tên loại sự cố
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                URL icon
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
                  {item.iconUrl ? (
                    <div className="h-10 w-10 overflow-hidden rounded-lg border border-gray-200 dark:border-gray-700">
                      <img
                        src={item.iconUrl}
                        alt={item.ten}
                        className="h-full w-full object-cover"
                        onError={(event) => {
                          event.currentTarget.style.display = "none";
                        }}
                      />
                    </div>
                  ) : (
                    <span className="text-theme-xs text-gray-500 dark:text-gray-400">
                      Không có icon
                    </span>
                  )}
                </TableCell>

                <TableCell className="px-5 py-4 text-start">
                  <p className="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                    {item.ten}
                  </p>
                </TableCell>

                <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                  {item.iconUrl ? (
                    <span className="line-clamp-1">{item.iconUrl}</span>
                  ) : (
                    "Chưa cập nhật"
                  )}
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
                      aria-label={`Sửa ${item.ten}`}
                    >
                      <PencilIcon className="size-4" />
                    </button>
                    <button
                      type="button"
                      onClick={() => onDeleteItem?.(item)}
                      className="inline-flex items-center justify-center w-8 h-8 text-error-600 border border-error-200 rounded-lg hover:bg-error-50 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                      aria-label={`Xóa ${item.ten}`}
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
                  colSpan={5}
                >
                  Chưa có loại sự cố nào.
                </td>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
