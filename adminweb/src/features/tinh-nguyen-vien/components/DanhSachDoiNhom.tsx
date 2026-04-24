import { useMemo } from "react";
import { PencilIcon, TrashBinIcon } from "@/icons";
import {
  mockDatabase,
  type DoiNhomTinhNguyenVienRecord,
  type VaiTroDoiNhom,
} from "@/data/schemaMockData";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import Badge from "@/components/ui/badge/Badge";

export interface TeamMember {
  tinhNguyenVienId?: number;
  ten: string;
  avatarUrl: string;
  vaiTro: VaiTroDoiNhom;
}

export interface TeamTableRow {
  id: number;
  tenDoiNhom: string;
  soDienThoai: string;
  diaChi: string;
  doiTruong: TeamMember;
  thanhVien: TeamMember[];
  trangThaiHoatDong: boolean;
  active: boolean;
}

interface DanhSachDoiNhomProps {
  teams?: TeamTableRow[];
  extraTeams?: TeamTableRow[];
  onEditTeam?: (team: TeamTableRow) => void;
  onDeleteTeam?: (team: TeamTableRow) => void;
  onChangeTeamActive?: (team: TeamTableRow, active: boolean) => void;
  updatingActiveTeamId?: number | null;
}

function getVaiTroLabel(vaiTro: VaiTroDoiNhom): string {
  if (vaiTro === "truong_nhom") return "Truong nhom";
  if (vaiTro === "pho_nhom") return "Pho nhom";
  return "Thanh vien";
}

function createMemberFromMapping(
  mapping: DoiNhomTinhNguyenVienRecord
): TeamMember | null {
  if (!mapping.tinh_nguyen_vien_id) return null;

  const tinhNguyenVien = mockDatabase.tinh_nguyen_vien.find(
    (item) => item.id === mapping.tinh_nguyen_vien_id
  );
  const nguoiDung = mockDatabase.nguoi_dung.find(
    (item) => item.id === tinhNguyenVien?.nguoi_dung_id
  );

  if (!nguoiDung || !nguoiDung.ten) return null;

  return {
    tinhNguyenVienId: mapping.tinh_nguyen_vien_id ?? undefined,
    ten: nguoiDung.ten,
    avatarUrl: nguoiDung.avatar_url ?? "/images/user/user-01.jpg",
    vaiTro: mapping.vai_tro ?? "thanh_vien",
  };
}

export default function DanhSachDoiNhom({
  teams,
  extraTeams = [],
  onEditTeam,
  onDeleteTeam,
  onChangeTeamActive,
  updatingActiveTeamId = null,
}: DanhSachDoiNhomProps) {
  const mockTeamRows = useMemo<TeamTableRow[]>(
    () =>
      mockDatabase.doi_nhom.map((team) => {
        const viTri = mockDatabase.vi_tri.find((item) => item.id === team.vi_tri_id);
        const doiNhomMappings = mockDatabase.doi_nhom_tinh_nguyen_vien.filter(
          (item) => item.doi_nhom_id === team.id
        );

        const thanhVien = doiNhomMappings
          .map((mapping) => createMemberFromMapping(mapping))
          .filter((item): item is TeamMember => item !== null);

        const doiTruong =
          thanhVien.find((item) => item.vaiTro === "truong_nhom") ??
          thanhVien[0] ?? {
            ten: "Chua phan cong",
            avatarUrl: "/images/user/user-01.jpg",
            vaiTro: "thanh_vien" as VaiTroDoiNhom,
          };

        return {
          id: team.id,
          tenDoiNhom: team.ten_doi_nhom,
          soDienThoai: team.so_dien_thoai ?? "",
          diaChi: viTri?.dia_chi ?? "Chua cap nhat",
          doiTruong,
          thanhVien,
          trangThaiHoatDong: Boolean(team.trang_thai_hoat_dong),
          active: Boolean(team.active),
        };
      }),
    []
  );

  const teamRows = useMemo<TeamTableRow[]>(() => {
    if (teams) {
      return teams;
    }

    if (extraTeams.length === 0) {
      return mockTeamRows;
    }

    const extraTeamIds = new Set(extraTeams.map((team) => team.id));
    const remainingMockTeams = mockTeamRows.filter((team) => !extraTeamIds.has(team.id));

    return [...extraTeams, ...remainingMockTeams];
  }, [extraTeams, mockTeamRows, teams]);

  const handleEdit = (team: TeamTableRow) => onEditTeam?.(team);
  const handleDelete = (team: TeamTableRow) => onDeleteTeam?.(team);
  const handleChangeActive = (team: TeamTableRow, active: boolean) =>
    onChangeTeamActive?.(team, active);

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
                Đội trưởng
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Tên đội nhóm
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Khu vực hoạt động
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Thành viên nhóm
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
            {teamRows.map((team) => {
              const isActive = Boolean(team.active && team.trangThaiHoatDong);
              const isUpdatingActive = updatingActiveTeamId === team.id;

              return (
                <TableRow key={team.id}>
                  <TableCell className="px-5 py-4 sm:px-6 text-start">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 overflow-hidden rounded-full">
                        <img
                          width={40}
                          height={40}
                          src={team.doiTruong.avatarUrl}
                          alt={team.doiTruong.ten}
                          className="size-10 object-cover"
                        />
                      </div>
                      <div>
                        <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                          {team.doiTruong.ten}
                        </span>
                        <span className="block text-gray-500 text-theme-xs dark:text-gray-400">
                          {getVaiTroLabel(team.doiTruong.vaiTro)}
                        </span>
                      </div>
                    </div>
                  </TableCell>

                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {team.tenDoiNhom}
                  </TableCell>

                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {team.diaChi}
                  </TableCell>

                  <TableCell className="px-4 py-3 text-start">
                    {team.thanhVien.length > 0 ? (
                      <div className="flex items-center gap-2">
                        <div className="flex -space-x-2">
                          {team.thanhVien.slice(0, 4).map((member, index) => (
                            <div
                              key={`${member.ten}-${index}`}
                              className="w-7 h-7 overflow-hidden border-2 border-white rounded-full dark:border-gray-900"
                            >
                              <img
                                width={28}
                                height={28}
                                src={member.avatarUrl}
                                alt={member.ten}
                                className="size-7 object-cover"
                              />
                            </div>
                          ))}
                        </div>
                        {team.thanhVien.length > 4 && (
                          <span className="text-theme-xs text-gray-500 dark:text-gray-400">
                            +{team.thanhVien.length - 4}
                          </span>
                        )}
                      </div>
                    ) : (
                      <span className="text-theme-sm text-gray-500 dark:text-gray-400">
                        Chưa có thành viên
                      </span>
                    )}
                  </TableCell>

                  <TableCell className="px-4 py-3 text-start text-theme-sm dark:text-gray-400">
                    {onChangeTeamActive ? (
                      <div className="inline-flex min-w-[160px] flex-col gap-1">
                        <select
                          value={isActive ? "active" : "inactive"}
                          disabled={isUpdatingActive}
                          onChange={(event) => {
                            const nextActive = event.target.value === "active";
                            if (nextActive === isActive) return;
                            handleChangeActive(team, nextActive);
                          }}
                          className="h-9 w-full rounded-lg border border-gray-300 bg-white px-3 text-xs text-gray-700 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        >
                          <option value="active">Đang hoạt động</option>
                          <option value="inactive">Tạm ngừng</option>
                        </select>
                        {isUpdatingActive && (
                          <span className="text-xs text-gray-500 dark:text-gray-400">
                            Đang cập nhật...
                          </span>
                        )}
                      </div>
                    ) : (
                      <Badge size="sm" color={isActive ? "success" : "error"}>
                        {isActive ? "Đang hoạt động" : "Tạm ngừng"}
                      </Badge>
                    )}
                  </TableCell>

                  <TableCell className="px-4 py-3">
                    <div className="flex items-center justify-center gap-2">
                      {onEditTeam && (
                        <button
                          type="button"
                          onClick={() => handleEdit(team)}
                          className="inline-flex items-center justify-center w-8 h-8 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-100 hover:text-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                          aria-label={`Sửa ${team.tenDoiNhom}`}
                        >
                          <PencilIcon className="size-4" />
                        </button>
                      )}

                      {onDeleteTeam && (
                        <button
                          type="button"
                          onClick={() => handleDelete(team)}
                          className="inline-flex items-center justify-center w-8 h-8 text-error-600 border border-error-200 rounded-lg hover:bg-error-50 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                          aria-label={`Xóa ${team.tenDoiNhom}`}
                        >
                          <TrashBinIcon className="size-4" />
                        </button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              );
            })}

            {teamRows.length === 0 && (
              <TableRow>
                <td
                  className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                  colSpan={6}
                >
                  Chưa có đội nhóm nào trong danh sách.
                </td>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
