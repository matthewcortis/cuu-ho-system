import { useMemo, useState } from "react";
import { PencilIcon, TrashBinIcon } from "../../../icons";
import {
  buildVolunteerTableRows,
  createVolunteerDatabase,
  getVaiTroLabel,
  type VolunteerTableRow,
} from "../../../data/tinhNguyenVienData";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";

type VolunteerStatusValue = "active" | "inactive";

interface DanhSachTinhNguyenVienProps {
  onEditVolunteer?: (volunteer: VolunteerTableRow) => void;
  onDeleteVolunteer?: (volunteer: VolunteerTableRow) => void;
}

const statusOptions: Array<{ value: VolunteerStatusValue; label: string }> = [
  { value: "active", label: "Active" },
  { value: "inactive", label: "Inactive" },
];

const statusClasses: Record<VolunteerStatusValue, string> = {
  active:
    "border-success-300 bg-success-50 text-success-700 dark:border-success-500/30 dark:bg-success-500/15 dark:text-success-400",
  inactive:
    "border-error-300 bg-error-50 text-error-700 dark:border-error-500/30 dark:bg-error-500/15 dark:text-error-400",
};

export default function DanhSachTinhNguyenVien({
  onEditVolunteer,
  onDeleteVolunteer,
}: DanhSachTinhNguyenVienProps) {
  const volunteers = useMemo(
    () => buildVolunteerTableRows(createVolunteerDatabase()),
    []
  );

  const [statusByVolunteerId, setStatusByVolunteerId] = useState<
    Record<number, VolunteerStatusValue>
  >(() =>
    Object.fromEntries(
      volunteers.map((volunteer) => [
        volunteer.id,
        volunteer.trangThai ? "active" : "inactive",
      ])
    )
  );

  const handleStatusChange = (
    volunteerId: number,
    nextStatus: VolunteerStatusValue
  ) => {
    setStatusByVolunteerId((prev) => ({
      ...prev,
      [volunteerId]: nextStatus,
    }));
  };

  const handleEdit = (volunteer: VolunteerTableRow) =>
    onEditVolunteer?.(volunteer);
  const handleDelete = (volunteer: VolunteerTableRow) =>
    onDeleteVolunteer?.(volunteer);

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="max-w-full overflow-x-auto custom-scrollbar">
        <Table className="min-w-[1100px]">
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
                Đội nhóm
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
                Trạng thái
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
              const currentStatus =
                statusByVolunteerId[volunteer.id] ??
                (volunteer.trangThai ? "active" : "inactive");

              return (
                <TableRow key={volunteer.id}>
                  <TableCell className="px-5 py-4 sm:px-6 text-start">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 overflow-hidden rounded-full">
                        <img
                          width={40}
                          height={40}
                          src={volunteer.avatarUrl}
                          alt={volunteer.ten}
                          className="size-10 object-cover"
                        />
                      </div>
                      <div>
                        <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                          {volunteer.ten}
                        </span>
                        <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                          {getVaiTroLabel(volunteer.vaiTro)}
                        </span>
                      </div>
                    </div>
                  </TableCell>

                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {volunteer.diaChi}
                  </TableCell>

                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {volunteer.doiNhom}
                  </TableCell>

                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {volunteer.soDienThoai}
                  </TableCell>

                  <TableCell className="px-4 py-3 text-start">
                    <select
                      value={currentStatus}
                      onChange={(event) =>
                        handleStatusChange(
                          volunteer.id,
                          event.target.value as VolunteerStatusValue
                        )
                      }
                      className={`rounded-full border px-3 py-1.5 text-theme-xs font-medium outline-none ${statusClasses[currentStatus]}`}
                      aria-label={`Trang thai cua ${volunteer.ten}`}
                    >
                      {statusOptions.map((status) => (
                        <option key={status.value} value={status.value}>
                          {status.label}
                        </option>
                      ))}
                    </select>
                  </TableCell>

                  <TableCell className="px-4 py-3">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        type="button"
                        onClick={() => handleEdit(volunteer)}
                        className="inline-flex items-center justify-center w-8 h-8 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-100 hover:text-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                        aria-label={`Sua ${volunteer.ten}`}
                      >
                        <PencilIcon className="size-4" />
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(volunteer)}
                        className="inline-flex items-center justify-center w-8 h-8 text-error-600 border border-error-200 rounded-lg hover:bg-error-50 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                        aria-label={`Xoa ${volunteer.ten}`}
                      >
                        <TrashBinIcon className="size-4" />
                      </button>
                    </div>
                  </TableCell>
                </TableRow>
              );
            })}

            {volunteers.length === 0 && (
              <TableRow>
                <td
                  className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                  colSpan={6}
                >
                  Chua co tinh nguyen vien nao trong danh sach.
                </td>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
