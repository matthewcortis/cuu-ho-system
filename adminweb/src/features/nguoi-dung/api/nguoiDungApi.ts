import type {
  DanhSachCuuTroRecord,
  DoiNhomRecord,
  DonViRecord,
  NguoiDungRecord,
  PhieuCuuTroRecord,
  TaiKhoanRecord,
  VatPhamRecord,
  ViTriRecord,
} from "@/data/schemaMockData";
import { supabase } from "@/utils/supabase";

export type NguoiDungDataSource = {
  nguoi_dung: NguoiDungRecord[];
  tai_khoan: TaiKhoanRecord[];
  vi_tri: ViTriRecord[];
  phieu_cuu_tro: PhieuCuuTroRecord[];
  danh_sach_cuu_tro: DanhSachCuuTroRecord[];
  vat_pham: VatPhamRecord[];
  don_vi: DonViRecord[];
  doi_nhom: DoiNhomRecord[];
};

async function fetchTableRows<T>(tableName: string): Promise<T[]> {
  const { data, error } = await supabase.from(tableName).select("*");

  if (error) {
    throw new Error(`Lay du lieu ${tableName} that bai: ${error.message}`);
  }

  return (data as T[]) ?? [];
}

export async function fetchNguoiDungDataSource(): Promise<NguoiDungDataSource> {
  const [
    nguoi_dung,
    tai_khoan,
    vi_tri,
    phieu_cuu_tro,
    danh_sach_cuu_tro,
    vat_pham,
    don_vi,
    doi_nhom,
  ] = await Promise.all([
    fetchTableRows<NguoiDungRecord>("nguoi_dung"),
    fetchTableRows<TaiKhoanRecord>("tai_khoan"),
    fetchTableRows<ViTriRecord>("vi_tri"),
    fetchTableRows<PhieuCuuTroRecord>("phieu_cuu_tro"),
    fetchTableRows<DanhSachCuuTroRecord>("danh_sach_cuu_tro"),
    fetchTableRows<VatPhamRecord>("vat_pham"),
    fetchTableRows<DonViRecord>("don_vi"),
    fetchTableRows<DoiNhomRecord>("doi_nhom"),
  ]);

  return {
    nguoi_dung,
    tai_khoan,
    vi_tri,
    phieu_cuu_tro,
    danh_sach_cuu_tro,
    vat_pham,
    don_vi,
    doi_nhom,
  };
}

