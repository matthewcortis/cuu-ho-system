import { useMemo, useState } from "react";
import { CheckLineIcon, TrashBinIcon } from "../../../icons";
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
import { Modal } from "../../ui/modal";

interface DuyetTinhNguyenVienProps {
  onEditVolunteer?: (volunteer: VolunteerTableRow) => void;
  onDeleteVolunteer?: (volunteer: VolunteerTableRow) => void;
}

interface TeamOption {
  id: number;
  tenDoiNhom: string;
  soDienThoai: string;
  diaChi: string;
  dangHoatDong: boolean;
}

export default function DuyetTinhNguyenVien({
  onEditVolunteer,
  onDeleteVolunteer,
}: DuyetTinhNguyenVienProps) {
  const database = useMemo(() => createVolunteerDatabase(), []);
  const volunteers = useMemo(() => buildVolunteerTableRows(database), [database]);
  const teamOptions = useMemo<TeamOption[]>(
    () =>
      database.doi_nhom.map((team) => {
        const viTri = database.vi_tri.find((item) => item.id === team.vi_tri_id);
        return {
          id: team.id,
          tenDoiNhom: team.ten_doi_nhom,
          soDienThoai: team.so_dien_thoai ?? "Chua cap nhat",
          diaChi: viTri?.dia_chi ?? "Chua cap nhat",
          dangHoatDong: Boolean(team.trang_thai_hoat_dong),
        };
      }),
    [database]
  );

  const [selectedVolunteer, setSelectedVolunteer] =
    useState<VolunteerTableRow | null>(null);
  const [isTeamDialogOpen, setIsTeamDialogOpen] = useState(false);
  const [selectedTeamIdByVolunteerId, setSelectedTeamIdByVolunteerId] =
    useState<Record<number, number>>({});

  const handleApprove = (volunteer: VolunteerTableRow) => {
    const selectedTeamId = selectedTeamIdByVolunteerId[volunteer.id];
    const selectedTeam = teamOptions.find((team) => team.id === selectedTeamId);
    if (!selectedTeam) return;

    onEditVolunteer?.({
      ...volunteer,
      doiNhom: selectedTeam.tenDoiNhom,
    });
  };
  const handleDelete = (volunteer: VolunteerTableRow) =>
    onDeleteVolunteer?.(volunteer);
  const handleOpenTeamDialog = (volunteer: VolunteerTableRow) => {
    setSelectedVolunteer(volunteer);
    setIsTeamDialogOpen(true);
  };
  const handleCloseTeamDialog = () => {
    setIsTeamDialogOpen(false);
    setSelectedVolunteer(null);
  };
  const handleSelectTeam = (teamId: number) => {
    if (!selectedVolunteer) return;

    setSelectedTeamIdByVolunteerId((prev) => ({
      ...prev,
      [selectedVolunteer.id]: teamId,
    }));
    handleCloseTeamDialog();
  };

  return (
    <>
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
        <div className="max-w-full overflow-x-auto custom-scrollbar">
          <Table className="min-w-[1000px]">
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
                  className="px-4 py-3 font-medium text-gray-500 text-center text-theme-xs dark:text-gray-400"
                >
                  Hành động
                </TableCell>
              </TableRow>
            </TableHeader>

            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {volunteers.map((volunteer) => {
                const selectedTeamId = selectedTeamIdByVolunteerId[volunteer.id];
                const selectedTeam = teamOptions.find(
                  (team) => team.id === selectedTeamId
                );

                return (
                  <TableRow key={volunteer.id}>
                    <TableCell className="px-5 py-4 sm:px-6 text-start">
                      <div className="flex items-start gap-3">
                        <div className="w-12 h-12 overflow-hidden rounded-full">
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
                            {getVaiTroLabel(volunteer.vaiTro)}
                          </span>
                          <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                            SĐT: {volunteer.soDienThoai}
                          </span>
                          <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                            Email: {volunteer.email}
                          </span>
                        </div>
                      </div>
                    </TableCell>

                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {volunteer.diaChi}
                    </TableCell>

                    <TableCell className="px-4 py-3 text-start">
                      {selectedTeam ? (
                        <div className="flex items-center gap-2">
                          <span className="inline-flex rounded-full border border-brand-200 bg-brand-50 px-3 py-1.5 text-theme-xs font-medium text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
                            {selectedTeam.tenDoiNhom}
                          </span>
                          <button
                            type="button"
                            onClick={() => handleOpenTeamDialog(volunteer)}
                            className="inline-flex items-center rounded-lg border border-gray-200 px-3 py-1.5 text-theme-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                          >
                            Chọn lại
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          onClick={() => handleOpenTeamDialog(volunteer)}
                          className="inline-flex items-center rounded-lg border border-brand-200 px-3 py-1.5 text-theme-xs font-medium text-brand-600 hover:bg-brand-50 dark:border-brand-500/30 dark:text-brand-400 dark:hover:bg-brand-500/10"
                        >
                          Xem danh sách đội nhóm
                        </button>
                      )}
                    </TableCell>

                    <TableCell className="px-4 py-3">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleApprove(volunteer)}
                          className="inline-flex items-center gap-1 rounded-lg bg-brand-500 px-3 py-2 text-theme-xs font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:bg-brand-300"
                          aria-label={`Chấp nhận ${volunteer.ten}`}
                          disabled={!selectedTeam}
                        >
                          <CheckLineIcon className="size-4" />
                          Chấp nhận
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDelete(volunteer)}
                          className="inline-flex items-center gap-1 rounded-lg border border-error-200 px-3 py-2 text-theme-xs font-medium text-error-600 hover:bg-error-50 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                          aria-label={`Xoa ${volunteer.ten}`}
                        >
                          <TrashBinIcon className="size-4" />
                          Xóa
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
                    colSpan={4}
                  >
                    Chưa có tình nguyện viên nào trong danh sách.
                  </td>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      <Modal
        isOpen={isTeamDialogOpen}
        onClose={handleCloseTeamDialog}
        className="max-w-[760px] m-4"
      >
        <div className="p-6 sm:p-7">
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Chọn đội nhóm
          </h3>
          <p className="mt-1 text-theme-sm text-gray-500 dark:text-gray-400">
            Chọn một đội để điều hướng tình nguyện viên sau khi duyệt.
          </p>

          {selectedVolunteer && (
            <div className="mt-4 rounded-xl border border-gray-200 bg-gray-50 p-4 dark:border-white/[0.08] dark:bg-white/[0.03]">
              <p className="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                {selectedVolunteer.ten}
              </p>
              <p className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                SĐT: {selectedVolunteer.soDienThoai}
              </p>
              <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                Email: {selectedVolunteer.email}
              </p>
              <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                Địa chỉ: {selectedVolunteer.diaChi}
              </p>
            </div>
          )}

          <div className="mt-4 max-h-[420px] overflow-y-auto pr-1 custom-scrollbar">
            <div className="grid gap-3 sm:grid-cols-2">
              {teamOptions.map((team) => {
                const currentSelectedTeamId = selectedVolunteer
                  ? selectedTeamIdByVolunteerId[selectedVolunteer.id]
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
            </div>
          </div>
        </div>
      </Modal>
    </>
  );
}
