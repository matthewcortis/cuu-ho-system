import { FormEvent, useEffect, useMemo, useState } from "react";
import { PencilIcon, TrashBinIcon } from "@/icons";
import { Modal } from "@/components/ui/modal";
import {
  fetchDaDuyetTinhNguyenVien,
  ganDoiNhomChoTinhNguyenVien,
  TinhNguyenVienApiError,
  type TinhNguyenVienDaDuyetItem,
  type VaiTroDoiNhom,
  xoaTinhNguyenVien,
} from "@/features/tinh-nguyen-vien/api/tinhNguyenVienApi";
import {
  DoiNhomApiError,
  fetchDoiNhomList,
  type DoiNhomDto,
} from "@/features/tinh-nguyen-vien/api/doiNhomApi";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

function getApiErrorMessage(error: unknown): string {
  if (error instanceof TinhNguyenVienApiError || error instanceof DoiNhomApiError) {
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

function getVaiTroLabel(vaiTro: VaiTroDoiNhom): string {
  if (vaiTro === "truong_nhom") {
    return "Trưởng nhóm";
  }
  if (vaiTro === "pho_nhom") {
    return "Phó nhóm";
  }
  return "Thành viên";
}

function isTeamAvailable(team: DoiNhomDto): boolean {
  return Boolean(team.active && team.trangThaiHoatDong);
}

function getTeamLabel(team: DoiNhomDto): string {
  const teamName = team.tenDoiNhom.trim();
  return teamName.length > 0 ? teamName : `Đội nhóm #${team.id}`;
}

export default function DanhSachTinhNguyenVien() {
  const [volunteers, setVolunteers] = useState<TinhNguyenVienDaDuyetItem[]>([]);
  const [teams, setTeams] = useState<DoiNhomDto[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isMutatingByVolunteerId, setIsMutatingByVolunteerId] = useState<
    Record<number, boolean>
  >({});

  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [selectedVolunteer, setSelectedVolunteer] =
    useState<TinhNguyenVienDaDuyetItem | null>(null);
  const [selectedTeamId, setSelectedTeamId] = useState("");
  const [selectedVaiTro, setSelectedVaiTro] = useState<VaiTroDoiNhom>("thanh_vien");
  const [isSubmittingAssign, setIsSubmittingAssign] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const loadInitialData = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const [volunteersResult, teamsResult] = await Promise.allSettled([
          fetchDaDuyetTinhNguyenVien(),
          fetchDoiNhomList(),
        ]);

        if (!isMounted) {
          return;
        }

        const loadErrors: string[] = [];

        if (volunteersResult.status === "fulfilled") {
          setVolunteers(volunteersResult.value);
        } else {
          loadErrors.push(
            `Danh sách tình nguyện viên: ${getApiErrorMessage(volunteersResult.reason)}`
          );
        }

        if (teamsResult.status === "fulfilled") {
          setTeams(teamsResult.value);
        } else {
          loadErrors.push(`Danh sách đội nhóm: ${getApiErrorMessage(teamsResult.reason)}`);
        }

        if (loadErrors.length > 0) {
          setLoadError(loadErrors.join(" | "));
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void loadInitialData();

    return () => {
      isMounted = false;
    };
  }, []);

  const teamNamesByVolunteerId = useMemo<Record<number, string[]>>(() => {
    const result: Record<number, string[]> = {};

    teams.forEach((team) => {
      const teamLabel = getTeamLabel(team);

      team.thanhViens.forEach((member) => {
        const volunteerId = member.tinhNguyenVienId;
        const existingNames = result[volunteerId] ?? [];

        if (!existingNames.includes(teamLabel)) {
          result[volunteerId] = [...existingNames, teamLabel];
        }
      });
    });

    return result;
  }, [teams]);

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

  const closeAssignModal = () => {
    setIsAssignModalOpen(false);
    setSelectedVolunteer(null);
    setSelectedTeamId("");
    setSelectedVaiTro("thanh_vien");
    setIsSubmittingAssign(false);
  };

  const handleOpenAssignModal = (volunteer: TinhNguyenVienDaDuyetItem) => {
    setActionError(null);
    setSuccessMessage(null);

    const teamAssigned = teams.find((team) =>
      team.thanhViens.some((member) => member.tinhNguyenVienId === volunteer.id)
    );
    const defaultTeam = teamAssigned ?? teams.find((team) => isTeamAvailable(team)) ?? teams[0];

    setSelectedVolunteer(volunteer);
    setSelectedTeamId(defaultTeam ? String(defaultTeam.id) : "");
    setSelectedVaiTro("thanh_vien");
    setIsAssignModalOpen(true);
  };

  const handleDeleteVolunteer = async (volunteer: TinhNguyenVienDaDuyetItem) => {
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
    setSuccessMessage(null);
    setMutatingState(volunteer.id, true);

    try {
      await xoaTinhNguyenVien(volunteer.id);
      setVolunteers((prev) => prev.filter((item) => item.id !== volunteer.id));

      if (selectedVolunteer?.id === volunteer.id) {
        closeAssignModal();
      }
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setMutatingState(volunteer.id, false);
    }
  };

  const handleAssignVolunteer = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedVolunteer || isSubmittingAssign) {
      return;
    }

    const doiNhomId = Number(selectedTeamId);
    if (!Number.isInteger(doiNhomId) || doiNhomId <= 0) {
      setActionError("Vui lòng chọn đội nhóm để điều phối.");
      return;
    }

    setActionError(null);
    setSuccessMessage(null);
    setIsSubmittingAssign(true);
    setMutatingState(selectedVolunteer.id, true);

    try {
      await ganDoiNhomChoTinhNguyenVien({
        tinhNguyenVienId: selectedVolunteer.id,
        doiNhomId,
        vaiTro: selectedVaiTro,
      });

      const refreshedTeams = await fetchDoiNhomList();
      setTeams(refreshedTeams);

      setSuccessMessage(
        `Đã điều phối ${selectedVolunteer.ten} vào ${
          refreshedTeams.find((team) => team.id === doiNhomId)?.tenDoiNhom || `đội #${doiNhomId}`
        } với vai trò ${getVaiTroLabel(selectedVaiTro).toLowerCase()}.`
      );
      closeAssignModal();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
      setIsSubmittingAssign(false);
    } finally {
      setMutatingState(selectedVolunteer.id, false);
    }
  };

  return (
    <>
      {loadError && (
        <div className="mb-4 rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
          Không thể lấy danh sách tình nguyện viên đã duyệt: {loadError}
        </div>
      )}

      {actionError && (
        <div className="mb-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-theme-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-300">
          {actionError}
        </div>
      )}

      {successMessage && (
        <div className="mb-4 rounded-lg border border-success-200 bg-success-50 px-4 py-3 text-theme-sm text-success-700 dark:border-success-500/30 dark:bg-success-500/10 dark:text-success-300">
          {successMessage}
        </div>
      )}

      {isLoading && (
        <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
          Đang tải danh sách tình nguyện viên đã duyệt...
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
        <div className="max-w-full overflow-x-auto custom-scrollbar">
          <Table className="min-w-[1140px]">
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
                  Đội nhóm
                </TableCell>
                <TableCell
                  isHeader
                  className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
                >
                  Trạng thái duyệt
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
                const teamNames = teamNamesByVolunteerId[volunteer.id] ?? [];

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
                        </div>
                      </div>
                    </TableCell>

                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      {volunteer.diaChi}
                    </TableCell>

                    <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                      <div>{volunteer.kyNang}</div>
                      {volunteer.ghiChu && volunteer.ghiChu !== "Khong co ghi chu" && (
                        <div className="mt-1 text-theme-xs text-gray-400 dark:text-gray-500">
                          Ghi chú: {volunteer.ghiChu}
                        </div>
                      )}
                    </TableCell>

                    <TableCell className="px-4 py-3 text-start">
                      {teamNames.length > 0 ? (
                        <div className="flex flex-wrap gap-1.5">
                          {teamNames.map((teamName) => (
                            <span
                              key={`${volunteer.id}-${teamName}`}
                              className="inline-flex rounded-full border border-gray-200 px-2.5 py-1 text-theme-xs text-gray-700 dark:border-gray-700 dark:text-gray-300"
                            >
                              {teamName}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-theme-sm text-gray-500 dark:text-gray-400">
                          Chưa phân đội
                        </span>
                      )}
                    </TableCell>

                    <TableCell className="px-4 py-3 text-start">
                      <span className="inline-flex rounded-full border border-success-300 bg-success-50 px-2.5 py-1 text-theme-xs font-medium text-success-700 dark:border-success-500/30 dark:bg-success-500/15 dark:text-success-400">
                        Đã duyệt
                      </span>
                      <div className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                        {extractSimpleDate(volunteer.thoiGianDuyet)}
                      </div>
                    </TableCell>

                    <TableCell className="px-4 py-3">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleOpenAssignModal(volunteer)}
                          className="inline-flex items-center justify-center w-8 h-8 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-100 hover:text-gray-800 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                          aria-label={`Điều phối ${volunteer.ten}`}
                          disabled={isMutating}
                        >
                          <PencilIcon className="size-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDeleteVolunteer(volunteer)}
                          className="inline-flex items-center justify-center w-8 h-8 text-error-600 border border-error-200 rounded-lg hover:bg-error-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                          aria-label={`Xóa ${volunteer.ten}`}
                          disabled={isMutating}
                        >
                          <TrashBinIcon className="size-4" />
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
                    colSpan={6}
                  >
                    Không có tình nguyện viên nào đã được duyệt.
                  </td>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      <Modal isOpen={isAssignModalOpen} onClose={closeAssignModal} className="m-4 max-w-[560px]">
        <div className="rounded-3xl bg-white p-6 dark:bg-gray-900">
          <h3 className="text-xl font-semibold text-gray-800 dark:text-white/90">
            Điều phối tình nguyện viên
          </h3>
          <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
            {selectedVolunteer
              ? `Chọn đội nhóm và vai trò cho ${selectedVolunteer.ten}.`
              : "Chọn đội nhóm và vai trò."}
          </p>

          <form className="mt-5 space-y-4" onSubmit={handleAssignVolunteer}>
            <div>
              <label
                htmlFor="doi-nhom-select"
                className="mb-1 block text-theme-sm font-medium text-gray-700 dark:text-gray-300"
              >
                Đội nhóm
              </label>
              <select
                id="doi-nhom-select"
                value={selectedTeamId}
                onChange={(event) => setSelectedTeamId(event.target.value)}
                disabled={isSubmittingAssign || teams.length === 0}
                className="h-11 w-full rounded-lg border border-gray-300 bg-white px-3 text-sm text-gray-700 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:bg-gray-800 dark:text-white/90"
              >
                <option value="">Chọn đội nhóm</option>
                {teams.map((team) => (
                  <option key={team.id} value={team.id}>
                    {getTeamLabel(team)}
                    {!isTeamAvailable(team) ? " - Tạm ngừng" : ""}
                  </option>
                ))}
              </select>
              {teams.length === 0 && (
                <p className="mt-1 text-theme-xs text-error-500 dark:text-error-400">
                  Chưa có đội nhóm nào để điều phối.
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="vai-tro-select"
                className="mb-1 block text-theme-sm font-medium text-gray-700 dark:text-gray-300"
              >
                Vai trò trong đội
              </label>
              <select
                id="vai-tro-select"
                value={selectedVaiTro}
                onChange={(event) => setSelectedVaiTro(event.target.value as VaiTroDoiNhom)}
                disabled={isSubmittingAssign}
                className="h-11 w-full rounded-lg border border-gray-300 bg-white px-3 text-sm text-gray-700 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:bg-gray-800 dark:text-white/90"
              >
                <option value="thanh_vien">Thành viên</option>
                <option value="pho_nhom">Phó nhóm</option>
                <option value="truong_nhom">Trưởng nhóm</option>
              </select>
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={closeAssignModal}
                className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                disabled={isSubmittingAssign}
              >
                Hủy
              </button>
              <button
                type="submit"
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:bg-brand-300"
                disabled={isSubmittingAssign || !selectedVolunteer || teams.length === 0}
              >
                {isSubmittingAssign ? "Đang điều phối..." : "Xác nhận điều phối"}
              </button>
            </div>
          </form>
        </div>
      </Modal>
    </>
  );
}
