import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import Badge from "@/components/ui/badge/Badge";
import logoPng from "@/icons/logo.png";

export type VatPhamTrangThai = "san_sang" | "ngung_cung_cap";

export interface DanhSachVatPhamItem {
  id: number;
  tenVatPham: string;
  nhomVatPhamId: number | null;
  nhomVatPham: string;
  donVi: string;
  soLuong: number;
  trangThai: VatPhamTrangThai;
  imageUrl: string | null;
  thieuAnh: boolean;
  createdAt: string;
}

interface DanhSachVatPhamTableProps {
  items: DanhSachVatPhamItem[];
}

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Chua cap nhat";
  return date.toLocaleDateString("vi-VN");
}

function getStatusLabel(status: VatPhamTrangThai): string {
  return status === "san_sang" ? "San sang" : "Ngung cung cap";
}

export default function DanhSachVatPhamTable({ items }: DanhSachVatPhamTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="max-w-full overflow-x-auto custom-scrollbar">
        <Table className="min-w-[1140px]">
          <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell
                isHeader
                className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Anh
              </TableCell>
              <TableCell
                isHeader
                className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Ten vat pham
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Nhom vat pham
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Don vi
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                So luong
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Trang thai
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Ngay tao
              </TableCell>
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {items.map((item) => (
              <TableRow key={item.id}>
                <TableCell className="px-5 py-4 text-start">
                  <div className="w-12 h-12 overflow-hidden rounded-lg border border-gray-200 dark:border-gray-700">
                    <img
                      src={item.imageUrl || logoPng}
                      alt={item.tenVatPham}
                      className="h-full w-full object-cover"
                      onError={(event) => {
                        event.currentTarget.src = logoPng;
                      }}
                    />
                  </div>
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
                  {item.donVi}
                </TableCell>
                <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                  {item.soLuong}
                </TableCell>
                <TableCell className="px-4 py-3 text-start">
                  <Badge
                    size="sm"
                    color={item.trangThai === "san_sang" ? "success" : "error"}
                  >
                    {getStatusLabel(item.trangThai)}
                  </Badge>
                </TableCell>
                <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                  {formatDate(item.createdAt)}
                </TableCell>
              </TableRow>
            ))}

            {items.length === 0 && (
              <TableRow>
                <td
                  className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                  colSpan={7}
                >
                  Khong tim thay vat pham phu hop voi bo loc.
                </td>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}


