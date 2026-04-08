export type VaiTroDoiNhom = "truong_nhom" | "pho_nhom" | "thanh_vien";

export interface BangTinRecord {
  id: number;
  tieu_de: string | null;
  noi_dung: string | null;
  tep_tin_id: number | null;
  vi_tri_id: number | null;
  nguoi_dung_id: string | null;
  trang_thai: boolean | null;
  created_at: string;
}

export interface DanhSachCuuTroRecord {
  id: number;
  created_at: string;
  ten: string | null;
  icon_url: string | null;
  vat_pham_id: number | null;
}

export interface DoiNhomRecord {
  id: number;
  ten_doi_nhom: string;
  so_dien_thoai: string | null;
  vi_tri_id: number | null;
  trang_thai_hoat_dong: boolean | null;
  active: boolean | null;
  created_at: string | null;
}

export interface DoiNhomTinhNguyenVienRecord {
  id: number;
  doi_nhom_id: number | null;
  tinh_nguyen_vien_id: number | null;
  vai_tro: VaiTroDoiNhom | null;
  created_at: string | null;
}

export interface DonViRecord {
  id: number;
  created_at: string;
  ten: string | null;
  ma_don_vi: string | null;
}

export interface NguoiDungRecord {
  id: string;
  tai_khoan_id: number | null;
  ten: string | null;
  sdt: string | null;
  vi_tri_id: number | null;
  avatar_url: string | null;
  created_at: string;
}

export interface NhomVatPhamRecord {
  id: number;
  ten: string | null;
  mo_ta: string | null;
  created_at: string;
}

export interface PhieuCuuTroRecord {
  id: number;
  danh_sach_cuu_tro_id: number;
  vi_tri_id: number | null;
  tep_tin_id: number | null;
  nguoi_dung_id: string | null;
  ho_ten: string | null;
  sdt: string | null;
  ghi_chu: string | null;
  created_at: string;
}

export interface TaiKhoanRecord {
  id: number;
  email: string | null;
  ten_dang_nhap: string | null;
  mat_khau: string | null;
  trang_thai: boolean | null;
  created_at: string;
}

export interface TepTinRecord {
  id: number;
  created_at: string;
  duong_dan: string | null;
  loai_tep_tin: string | null;
}

export interface TinhNguyenVienRecord {
  id: number;
  nguoi_dung_id: string | null;
  thoi_gian: string | null;
  ghi_chu: string | null;
  co_the_giup: string | null;
  trang_thai: boolean | null;
  created_at: string;
}

export interface VatPhamRecord {
  id: number;
  ten_vat_pham: string | null;
  created_at: string;
  so_luong: number | null;
  don_vi_id: number | null;
  nhom_vat_pham_id: number | null;
}

export interface ViTriRecord {
  id: number;
  lat: string | null;
  long: string | null;
  created_at: string;
  dia_chi: string | null;
}

export interface MockDatabase {
  bang_tin: BangTinRecord[];
  danh_sach_cuu_tro: DanhSachCuuTroRecord[];
  doi_nhom: DoiNhomRecord[];
  doi_nhom_tinh_nguyen_vien: DoiNhomTinhNguyenVienRecord[];
  don_vi: DonViRecord[];
  nguoi_dung: NguoiDungRecord[];
  nhom_vat_pham: NhomVatPhamRecord[];
  phieu_cuu_tro: PhieuCuuTroRecord[];
  tai_khoan: TaiKhoanRecord[];
  tep_tin: TepTinRecord[];
  tinh_nguyen_vien: TinhNguyenVienRecord[];
  vat_pham: VatPhamRecord[];
  vi_tri: ViTriRecord[];
}

const baseTime = "2026-03-21T09:00:00Z";

export const mockDatabase: MockDatabase = {
  vi_tri: [
    {
      id: 1,
      lat: "10.7769",
      long: "106.7009",
      created_at: baseTime,
      dia_chi: "Phuong Ben Nghe, Quan 1, TP. Ho Chi Minh",
    },
    {
      id: 2,
      lat: "10.8033",
      long: "106.6968",
      created_at: baseTime,
      dia_chi: "Phuong 12, Quan Binh Thanh, TP. Ho Chi Minh",
    },
    {
      id: 3,
      lat: "10.8492",
      long: "106.7718",
      created_at: baseTime,
      dia_chi: "Phuong Linh Trung, TP. Thu Duc",
    },
    {
      id: 4,
      lat: "10.8373",
      long: "106.6655",
      created_at: baseTime,
      dia_chi: "Phuong 5, Quan Go Vap, TP. Ho Chi Minh",
    },
    {
      id: 5,
      lat: "10.7526",
      long: "106.6686",
      created_at: baseTime,
      dia_chi: "Phuong 10, Quan 5, TP. Ho Chi Minh",
    },
    {
      id: 6,
      lat: "10.7398",
      long: "106.6986",
      created_at: baseTime,
      dia_chi: "Phuong Tan Hung, Quan 7, TP. Ho Chi Minh",
    },
  ],

  tep_tin: [
    {
      id: 1,
      created_at: baseTime,
      duong_dan: "/uploads/bang_tin_1.jpg",
      loai_tep_tin: "image/jpeg",
    },
    {
      id: 2,
      created_at: baseTime,
      duong_dan: "/uploads/bang_tin_2.jpg",
      loai_tep_tin: "image/jpeg",
    },
    {
      id: 3,
      created_at: baseTime,
      duong_dan: "/uploads/phieu_1.pdf",
      loai_tep_tin: "application/pdf",
    },
    {
      id: 4,
      created_at: baseTime,
      duong_dan: "/uploads/phieu_2.pdf",
      loai_tep_tin: "application/pdf",
    },
    {
      id: 5,
      created_at: baseTime,
      duong_dan: "/uploads/doi_nhom_1.png",
      loai_tep_tin: "image/png",
    },
    {
      id: 6,
      created_at: baseTime,
      duong_dan: "/uploads/thong_bao_1.jpg",
      loai_tep_tin: "image/jpeg",
    },
  ],

  tai_khoan: [
    {
      id: 1,
      email: "an.nguyen@example.com",
      ten_dang_nhap: "an.nguyen",
      mat_khau: "hashed_password_1",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 2,
      email: "linh.tran@example.com",
      ten_dang_nhap: "linh.tran",
      mat_khau: "hashed_password_2",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 3,
      email: "khoa.le@example.com",
      ten_dang_nhap: "khoa.le",
      mat_khau: "hashed_password_3",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 4,
      email: "mai.pham@example.com",
      ten_dang_nhap: "mai.pham",
      mat_khau: "hashed_password_4",
      trang_thai: false,
      created_at: baseTime,
    },
    {
      id: 5,
      email: "admin@example.com",
      ten_dang_nhap: "admin",
      mat_khau: "hashed_password_5",
      trang_thai: true,
      created_at: baseTime,
    },
  ],

  nguoi_dung: [
    {
      id: "f6bc5c4b-9f13-4ea7-a7ab-a0d7c96984d1",
      tai_khoan_id: 1,
      ten: "Nguyen Van An",
      sdt: "0901 234 567",
      vi_tri_id: 1,
      avatar_url: "/images/user/user-17.jpg",
      created_at: baseTime,
    },
    {
      id: "820cc7b5-b87f-4d17-8f3d-60c015f95ad1",
      tai_khoan_id: 2,
      ten: "Tran Thi Linh",
      sdt: "0938 111 222",
      vi_tri_id: 2,
      avatar_url: "/images/user/user-18.jpg",
      created_at: baseTime,
    },
    {
      id: "ac057e66-c95d-4f0f-b425-bce4f6b4164a",
      tai_khoan_id: 3,
      ten: "Le Minh Khoa",
      sdt: "0977 888 999",
      vi_tri_id: 3,
      avatar_url: "/images/user/user-19.jpg",
      created_at: baseTime,
    },
    {
      id: "6f3f25d0-b021-46c2-b542-c0ac526ca258",
      tai_khoan_id: 4,
      ten: "Pham Ngoc Mai",
      sdt: "0907 456 789",
      vi_tri_id: 4,
      avatar_url: "/images/user/user-20.jpg",
      created_at: baseTime,
    },
    {
      id: "ea3f2d89-f17f-4b5f-a9c9-7cf17a5a902a",
      tai_khoan_id: 5,
      ten: "Vo Thanh Dat",
      sdt: "0915 222 444",
      vi_tri_id: 5,
      avatar_url: "/images/user/user-21.jpg",
      created_at: baseTime,
    },
  ],

  tinh_nguyen_vien: [
    {
      id: 1,
      nguoi_dung_id: "f6bc5c4b-9f13-4ea7-a7ab-a0d7c96984d1",
      thoi_gian: null,
      ghi_chu: "Co the tham gia cuoi tuan",
      co_the_giup: "hau_can",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 2,
      nguoi_dung_id: "820cc7b5-b87f-4d17-8f3d-60c015f95ad1",
      thoi_gian: null,
      ghi_chu: "Co kinh nghiem y te co ban",
      co_the_giup: "y_te",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 3,
      nguoi_dung_id: "ac057e66-c95d-4f0f-b425-bce4f6b4164a",
      thoi_gian: null,
      ghi_chu: "Phu trach truyen thong",
      co_the_giup: "truyen_thong",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 4,
      nguoi_dung_id: "6f3f25d0-b021-46c2-b542-c0ac526ca258",
      thoi_gian: null,
      ghi_chu: "Tam ngung tham gia 2 tuan",
      co_the_giup: "dieu_phoi",
      trang_thai: false,
      created_at: baseTime,
    },
  ],

  doi_nhom: [
    {
      id: 1,
      ten_doi_nhom: "Doi Hau Can",
      so_dien_thoai: "0901 111 111",
      vi_tri_id: 1,
      trang_thai_hoat_dong: true,
      active: true,
      created_at: baseTime,
    },
    {
      id: 2,
      ten_doi_nhom: "Doi Y Te",
      so_dien_thoai: "0902 222 222",
      vi_tri_id: 2,
      trang_thai_hoat_dong: true,
      active: true,
      created_at: baseTime,
    },
    {
      id: 3,
      ten_doi_nhom: "Doi Truyen Thong",
      so_dien_thoai: "0903 333 333",
      vi_tri_id: 3,
      trang_thai_hoat_dong: true,
      active: true,
      created_at: baseTime,
    },
    {
      id: 4,
      ten_doi_nhom: "Doi Dieu Phoi",
      so_dien_thoai: "0904 444 444",
      vi_tri_id: 4,
      trang_thai_hoat_dong: true,
      active: true,
      created_at: baseTime,
    }
    ,
    {
      id: 5,
      ten_doi_nhom: "Doi Truyen Thong",
      so_dien_thoai: "0903 333 333",
      vi_tri_id: 3,
      trang_thai_hoat_dong: true,
      active: true,
      created_at: baseTime,
    },
    {
      id: 6,
      ten_doi_nhom: "Doi Dieu Phoi",
      so_dien_thoai: "0904 444 444",
      vi_tri_id: 4,
      trang_thai_hoat_dong: true,
      active: true,
      created_at: baseTime,
    },
  ],

  doi_nhom_tinh_nguyen_vien: [
    {
      id: 1,
      doi_nhom_id: 1,
      tinh_nguyen_vien_id: 1,
      vai_tro: "truong_nhom",
      created_at: baseTime,
    },
    {
      id: 2,
      doi_nhom_id: 2,
      tinh_nguyen_vien_id: 2,
      vai_tro: "pho_nhom",
      created_at: baseTime,
    },
    {
      id: 3,
      doi_nhom_id: 3,
      tinh_nguyen_vien_id: 3,
      vai_tro: "thanh_vien",
      created_at: baseTime,
    },
    {
      id: 4,
      doi_nhom_id: 4,
      tinh_nguyen_vien_id: 4,
      vai_tro: "thanh_vien",
      created_at: baseTime,
    },
  ],

  don_vi: [
    {
      id: 1,
      created_at: baseTime,
      ten: "Thung",
      ma_don_vi: "THUNG",
    },
    {
      id: 2,
      created_at: baseTime,
      ten: "Chai",
      ma_don_vi: "CHAI",
    },
    {
      id: 3,
      created_at: baseTime,
      ten: "Goi",
      ma_don_vi: "GOI",
    },
  ],

  nhom_vat_pham: [
    {
      id: 1,
      ten: "Nhu yeu pham",
      mo_ta: "Vat pham cho sinh hoat co ban",
      created_at: baseTime,
    },
    {
      id: 2,
      ten: "Y te",
      mo_ta: "Vat tu ho tro so cuu",
      created_at: baseTime,
    },
    {
      id: 3,
      ten: "Thuc pham",
      mo_ta: "Hang hoa tiep te an uong",
      created_at: baseTime,
    },
  ],

  vat_pham: [
    {
      id: 1,
      ten_vat_pham: "Nuoc uong dong chai",
      created_at: baseTime,
      so_luong: 120,
      don_vi_id: 2,
      nhom_vat_pham_id: 1,
    },
    {
      id: 2,
      ten_vat_pham: "Mi goi",
      created_at: baseTime,
      so_luong: 300,
      don_vi_id: 3,
      nhom_vat_pham_id: 3,
    },
    {
      id: 3,
      ten_vat_pham: "Hop so cuu",
      created_at: baseTime,
      so_luong: 50,
      don_vi_id: 1,
      nhom_vat_pham_id: 2,
    },
    {
      id: 4,
      ten_vat_pham: "Sua hop",
      created_at: baseTime,
      so_luong: 80,
      don_vi_id: 1,
      nhom_vat_pham_id: 3,
    },
    {
      id: 5,
      ten_vat_pham: "Ao mua",
      created_at: baseTime,
      so_luong: 200,
      don_vi_id: 3,
      nhom_vat_pham_id: 1,
    },
  ],

  danh_sach_cuu_tro: [
    {
      id: 1,
      created_at: baseTime,
      ten: "Nuoc uong 24 chai",
      icon_url: "/images/icons/file-image.svg",
      vat_pham_id: 1,
    },
    {
      id: 2,
      created_at: baseTime,
      ten: "Mi goi 30 goi",
      icon_url: "/images/icons/file-image.svg",
      vat_pham_id: 2,
    },
    {
      id: 3,
      created_at: baseTime,
      ten: "Hop so cuu y te",
      icon_url: "/images/icons/file-image.svg",
      vat_pham_id: 3,
    },
    {
      id: 4,
      created_at: baseTime,
      ten: "Ao mua 10 bo",
      icon_url: "/images/icons/file-image.svg",
      vat_pham_id: 5,
    },
  ],

  phieu_cuu_tro: [
    {
      id: 1,
      danh_sach_cuu_tro_id: 1,
      vi_tri_id: 6,
      tep_tin_id: 3,
      nguoi_dung_id: "ea3f2d89-f17f-4b5f-a9c9-7cf17a5a902a",
      ho_ten: "Le Thi Hoa",
      sdt: "0909 555 000",
      ghi_chu: "Ho gia dinh 5 nguoi",
      created_at: baseTime,
    },
    {
      id: 2,
      danh_sach_cuu_tro_id: 2,
      vi_tri_id: 5,
      tep_tin_id: 4,
      nguoi_dung_id: "f6bc5c4b-9f13-4ea7-a7ab-a0d7c96984d1",
      ho_ten: "Tran Van Son",
      sdt: "0911 345 678",
      ghi_chu: "Can tiep te trong ngay",
      created_at: baseTime,
    },
    {
      id: 3,
      danh_sach_cuu_tro_id: 3,
      vi_tri_id: 2,
      tep_tin_id: null,
      nguoi_dung_id: "820cc7b5-b87f-4d17-8f3d-60c015f95ad1",
      ho_ten: "Do Thi Thu",
      sdt: "0933 000 123",
      ghi_chu: "Can bo dung cu so cuu",
      created_at: baseTime,
    },
  ],

  bang_tin: [
    {
      id: 1,
      tieu_de: "Thong bao tap trung tinh nguyen vien",
      noi_dung: "Tat ca tinh nguyen vien co mat luc 08:00 tai tru so doi.",
      tep_tin_id: 1,
      vi_tri_id: 1,
      nguoi_dung_id: "f6bc5c4b-9f13-4ea7-a7ab-a0d7c96984d1",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 2,
      tieu_de: "Cap nhat diem phat qua",
      noi_dung: "Bo sung diem phat qua tai Quan 7 trong chieu nay.",
      tep_tin_id: 2,
      vi_tri_id: 6,
      nguoi_dung_id: "820cc7b5-b87f-4d17-8f3d-60c015f95ad1",
      trang_thai: true,
      created_at: baseTime,
    },
    {
      id: 3,
      tieu_de: "Thong bao tong ket",
      noi_dung: "Tong ket so lieu cuu tro tuan nay vao 17:00 thu Bay.",
      tep_tin_id: 6,
      vi_tri_id: 3,
      nguoi_dung_id: "ac057e66-c95d-4f0f-b425-bce4f6b4164a",
      trang_thai: false,
      created_at: baseTime,
    },
  ],
};
