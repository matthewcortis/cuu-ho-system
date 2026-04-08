import { useMemo } from "react";
import { PencilIcon, TrashBinIcon } from "../../../icons";
import {
  mockDatabase,
  type DoiNhomTinhNguyenVienRecord,
  type VaiTroDoiNhom,
} from "../../../data/schemaMockData";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";
import Badge from "../../ui/badge/Badge";

interface TeamMember {
  ten: string;
  avatarUrl: string;
  vaiTro: VaiTroDoiNhom;
}

interface TeamTableRow {
  id: number;
  tenDoiNhom: string;
  diaChi: string;
  doiTruong: TeamMember;
  thanhVien: TeamMember[];
  trangThaiHoatDong: boolean;
}

interface DanhSachDoiNhomProps {
  onEditTeam?: (team: TeamTableRow) => void;
  onDeleteTeam?: (team: TeamTableRow) => void;
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
    ten: nguoiDung.ten,
    avatarUrl: nguoiDung.avatar_url ?? "/images/user/user-01.jpg",
    vaiTro: mapping.vai_tro ?? "thanh_vien",
  };
}

export default function DanhSachDoiNhom({
  onEditTeam,
  onDeleteTeam,
}: DanhSachDoiNhomProps) {
  const teamRows = useMemo<TeamTableRow[]>(
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
          diaChi: viTri?.dia_chi ?? "Chua cap nhat",
          doiTruong,
          thanhVien,
          trangThaiHoatDong: Boolean(team.trang_thai_hoat_dong),
        };
      }),
    []
  );

  const handleEdit = (team: TeamTableRow) => onEditTeam?.(team);
  const handleDelete = (team: TeamTableRow) => onDeleteTeam?.(team);

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
                Doi truong
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Ten doi nhom
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Dia chi nhom
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Thanh vien nhom
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400"
              >
                Trang thai
              </TableCell>
              <TableCell
                isHeader
                className="px-4 py-3 font-medium text-gray-500 text-center text-theme-xs dark:text-gray-400"
              >
                Hanh dong
              </TableCell>
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {teamRows.map((team) => (
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
                      Chua co thanh vien
                    </span>
                  )}
                </TableCell>

                <TableCell className="px-4 py-3 text-start text-theme-sm dark:text-gray-400">
                  <Badge
                    size="sm"
                    color={team.trangThaiHoatDong ? "success" : "error"}
                  >
                    {team.trangThaiHoatDong ? "Dang hoat dong" : "Tam ngung"}
                  </Badge>
                </TableCell>

                <TableCell className="px-4 py-3">
                  <div className="flex items-center justify-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleEdit(team)}
                      className="inline-flex items-center justify-center w-8 h-8 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-100 hover:text-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                      aria-label={`Sua ${team.tenDoiNhom}`}
                    >
                      <PencilIcon className="size-4" />
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(team)}
                      className="inline-flex items-center justify-center w-8 h-8 text-error-600 border border-error-200 rounded-lg hover:bg-error-50 dark:border-error-500/30 dark:text-error-400 dark:hover:bg-error-500/10"
                      aria-label={`Xoa ${team.tenDoiNhom}`}
                    >
                      <TrashBinIcon className="size-4" />
                    </button>
                  </div>
                </TableCell>
              </TableRow>
            ))}

            {teamRows.length === 0 && (
              <TableRow>
                <td
                  className="px-5 py-10 text-center text-gray-500 text-theme-sm dark:text-gray-400"
                  colSpan={6}
                >
                  Chua co doi nhom nao trong danh sach.
                </td>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
