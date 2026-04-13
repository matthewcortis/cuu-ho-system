import {
  mockDatabase,
  type DoiNhomTinhNguyenVienRecord,
  type MockDatabase,
} from "@/data/schemaMockData";

export type VaiTroTinhNguyenVien = DoiNhomTinhNguyenVienRecord["vai_tro"];
export type VaiTroTinhNguyenVienValue = Exclude<VaiTroTinhNguyenVien, null>;

type VolunteerDatabase = Pick<
  MockDatabase,
  | "vi_tri"
  | "nguoi_dung"
  | "tai_khoan"
  | "tinh_nguyen_vien"
  | "doi_nhom"
  | "doi_nhom_tinh_nguyen_vien"
>;

export interface VolunteerTableRow {
  id: number;
  ten: string;
  avatarUrl: string;
  vaiTro: VaiTroTinhNguyenVienValue;
  email: string;
  diaChi: string;
  doiNhom: string;
  soDienThoai: string;
  trangThai: boolean;
}

function cloneRows<T extends object>(rows: T[]): T[] {
  return rows.map((row) => ({ ...row }));
}

export function createVolunteerDatabase(): VolunteerDatabase {
  return {
    vi_tri: cloneRows(mockDatabase.vi_tri),
    nguoi_dung: cloneRows(mockDatabase.nguoi_dung),
    tai_khoan: cloneRows(mockDatabase.tai_khoan),
    tinh_nguyen_vien: cloneRows(mockDatabase.tinh_nguyen_vien),
    doi_nhom: cloneRows(mockDatabase.doi_nhom),
    doi_nhom_tinh_nguyen_vien: cloneRows(mockDatabase.doi_nhom_tinh_nguyen_vien),
  };
}

export function buildVolunteerTableRows(
  database: VolunteerDatabase
): VolunteerTableRow[] {
  return database.tinh_nguyen_vien
    .map((tinhNguyenVien) => {
      const nguoiDung = database.nguoi_dung.find(
        (item) => item.id === tinhNguyenVien.nguoi_dung_id
      );
      const taiKhoan = database.tai_khoan.find(
        (item) => item.id === nguoiDung?.tai_khoan_id
      );
      const viTri = database.vi_tri.find(
        (item) => item.id === nguoiDung?.vi_tri_id
      );
      const doiNhomMapping = database.doi_nhom_tinh_nguyen_vien.find(
        (item) => item.tinh_nguyen_vien_id === tinhNguyenVien.id
      );
      const doiNhom = database.doi_nhom.find(
        (item) => item.id === doiNhomMapping?.doi_nhom_id
      );

      if (!nguoiDung || !nguoiDung.ten) return null;

      return {
        id: tinhNguyenVien.id,
        ten: nguoiDung.ten,
        avatarUrl: nguoiDung.avatar_url ?? "/images/user/user-01.jpg",
        vaiTro: doiNhomMapping?.vai_tro ?? "thanh_vien",
        email: taiKhoan?.email ?? "Chua cap nhat",
        diaChi: viTri?.dia_chi ?? "Chua cap nhat",
        doiNhom: doiNhom?.ten_doi_nhom ?? "Chua phan doi",
        soDienThoai: nguoiDung.sdt ?? "Chua cap nhat",
        trangThai: Boolean(tinhNguyenVien.trang_thai),
      };
    })
    .filter((item): item is VolunteerTableRow => item !== null);
}

export function getVolunteerTableRows(): VolunteerTableRow[] {
  const database = createVolunteerDatabase();
  return buildVolunteerTableRows(database);
}

export function getVaiTroLabel(vaiTro: VaiTroTinhNguyenVienValue): string {
  if (vaiTro === "truong_nhom") return "Truong nhom";
  if (vaiTro === "pho_nhom") return "Pho nhom";
  return "Thanh vien";
}

