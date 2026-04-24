Mục đích ứng dụng là dùng để cứu trợ người bị lũ lụt có 3 đối tượng ADMIN, tình nguyện viên, khách hàng 

Khách hàng tại vùng lũ lụt sẽ gửi một phiếu hỗ trợ gồm các thông tin như tên, số điện thoại, khu vực địa chỉ và ADMIN sẽ nhận được thông tin này 

nhiệm vụ ADMIN quản lý khách hàng, phiếu hỗ trợ, vật phẩm, tình nguyện viên. ADMIN nhận được phiếu hỗ trợ sẽ điều động đội tình nguyện viên đến nơi ứng cứu 

với tình nguyện viên chỉ có đội trưởng mới có quyền CẬP NHẬT TRẠNG THÁI, chat với khách hàng 


-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.bang_tin (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  tieu_de text,
  noi_dung text,
  tep_tin_id bigint,
  vi_tri_id bigint,
  nguoi_dung_id uuid,
  trang_thai boolean,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT bang_tin_pkey PRIMARY KEY (id),
  CONSTRAINT bang_tin_tep_tin_id_fkey FOREIGN KEY (tep_tin_id) REFERENCES public.tep_tin(id),
  CONSTRAINT bang_tin_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id),
  CONSTRAINT bang_tin_nguoi_dung_id_fkey FOREIGN KEY (nguoi_dung_id) REFERENCES public.nguoi_dung(id)
);
CREATE TABLE public.danh_sach_cuu_tro (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten text,
  icon_url text,
  vat_pham_id bigint,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT danh_sach_cuu_tro_pkey PRIMARY KEY (id),
  CONSTRAINT danh_sach_cuu_tro_vat_pham_id_fkey FOREIGN KEY (vat_pham_id) REFERENCES public.vat_pham(id)
);
CREATE TABLE public.doi_nhom (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten_doi_nhom character varying,
  so_dien_thoai character varying,
  vi_tri_id bigint,
  trang_thai_hoat_dong boolean DEFAULT true,
  active boolean DEFAULT true,
  trang_thai text DEFAULT 'idle'::text CHECK (trang_thai = ANY (ARRAY['idle'::text, 'busy'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT doi_nhom_pkey PRIMARY KEY (id),
  CONSTRAINT doi_nhom_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id)
);
CREATE TABLE public.doi_nhom_tinh_nguyen_vien (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  doi_nhom_id bigint,
  tinh_nguyen_vien_id bigint,
  vai_tro text CHECK (vai_tro = ANY (ARRAY['truong_nhom'::text, 'pho_nhom'::text, 'thanh_vien'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT doi_nhom_tinh_nguyen_vien_pkey PRIMARY KEY (id),
  CONSTRAINT doi_nhom_tinh_nguyen_vien_doi_nhom_id_fkey FOREIGN KEY (doi_nhom_id) REFERENCES public.doi_nhom(id),
  CONSTRAINT doi_nhom_tinh_nguyen_vien_tinh_nguyen_vien_id_fkey FOREIGN KEY (tinh_nguyen_vien_id) REFERENCES public.tinh_nguyen_vien(id)
);
CREATE TABLE public.don_vi (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten text,
  ma_don_vi text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT don_vi_pkey PRIMARY KEY (id)
);
CREATE TABLE public.khai_bao (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT khai_bao_pkey PRIMARY KEY (id)
);
CREATE TABLE public.nguoi_dung (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  tai_khoan_id bigint,
  ten text,
  sdt text,
  vi_tri_id bigint,
  avatar_url text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT nguoi_dung_pkey PRIMARY KEY (id),
  CONSTRAINT nguoi_dung_tai_khoan_id_fkey FOREIGN KEY (tai_khoan_id) REFERENCES public.tai_khoan(id),
  CONSTRAINT nguoi_dung_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id)
);
CREATE TABLE public.nguoi_dung_phan_quyen (
  nguoi_dung_id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phan_quyen_id bigint NOT NULL,
  CONSTRAINT nguoi_dung_phan_quyen_pkey PRIMARY KEY (nguoi_dung_id),
  CONSTRAINT nguoi_dung_phan_quyen_phan_quyen_id_fkey FOREIGN KEY (phan_quyen_id) REFERENCES public.phan_quyen(id)
);
CREATE TABLE public.nhom_vat_pham (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten text,
  mo_ta text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT nhom_vat_pham_pkey PRIMARY KEY (id)
);
CREATE TABLE public.phan_cong (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phieu_cuu_tro_id bigint,
  doi_nhom_id bigint,
  assigned_at timestamp with time zone DEFAULT now(),
  trang_thai text DEFAULT 'assigned'::text CHECK (trang_thai = ANY (ARRAY['assigned'::text, 'in_progress'::text, 'completed'::text])),
  CONSTRAINT phan_cong_pkey PRIMARY KEY (id),
  CONSTRAINT phan_cong_phieu_cuu_tro_id_fkey FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT phan_cong_doi_nhom_id_fkey FOREIGN KEY (doi_nhom_id) REFERENCES public.doi_nhom(id)
);
CREATE TABLE public.phan_quyen (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten text,
  mo_ta text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT phan_quyen_pkey PRIMARY KEY (id)
);
CREATE TABLE chi_tiet_cuu_tro (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  phieu_cuu_tro_id bigint,
  vat_pham_id bigint,
  so_luong integer,
  ghi_chu text,
  created_at timestamp DEFAULT now(),

  CONSTRAINT fk_phieu
  FOREIGN KEY (phieu_cuu_tro_id)
  REFERENCES phieu_cuu_tro(id),

  CONSTRAINT fk_vat_pham
  FOREIGN KEY (vat_pham_id)
  REFERENCES vat_pham(id)
);
CREATE TABLE public.phieu_cuu_tro (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  danh_sach_cuu_tro_id bigint,
  vi_tri_id bigint,
  tep_tin_id bigint,
  nguoi_dung_id uuid,
  ho_ten text,
  sdt text,
  ghi_chu text,
  trang_thai text DEFAULT 'pending'::text CHECK (trang_thai = ANY (ARRAY['pending'::text, 'assigned'::text, 'processing'::text, 'done'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT phieu_cuu_tro_pkey PRIMARY KEY (id),
  CONSTRAINT phieu_cuu_tro_danh_sach_cuu_tro_id_fkey FOREIGN KEY (danh_sach_cuu_tro_id) REFERENCES public.danh_sach_cuu_tro(id),
  CONSTRAINT phieu_cuu_tro_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id),
  CONSTRAINT phieu_cuu_tro_tep_tin_id_fkey FOREIGN KEY (tep_tin_id) REFERENCES public.tep_tin(id),
  CONSTRAINT phieu_cuu_tro_nguoi_dung_id_fkey FOREIGN KEY (nguoi_dung_id) REFERENCES public.nguoi_dung(id)
);
CREATE TABLE public.so_luong_vat_pham (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  khai_bao_id bigint,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  vat_pham_id bigint,
  so_luong smallint,
  CONSTRAINT so_luong_vat_pham_pkey PRIMARY KEY (id),
  CONSTRAINT so_luong_vat_pham_khai_bao_id_fkey FOREIGN KEY (khai_bao_id) REFERENCES public.khai_bao(id)
);
CREATE TABLE public.tai_khoan (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  email text,
  ten_dang_nhap text,
  mat_khau text,
  trang_thai boolean,
  vai_tro text DEFAULT 'USER'::text CHECK (vai_tro = ANY (ARRAY['ADMIN'::text, 'USER'::text, 'VOLUNTEER'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tai_khoan_pkey PRIMARY KEY (id)
);
CREATE TABLE public.tep_tin (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  duong_dan text,
  loai_tep_tin text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tep_tin_pkey PRIMARY KEY (id)
);
CREATE TABLE public.tin_nhan (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phieu_cuu_tro_id bigint,
  sender_id uuid,
  vi_tri_id bigint,
  noi_dung text,
  created_at timestamp with time zone DEFAULT now(),
  loai_tin_nhan text DEFAULT 'text'::text,
  media_url text,
  media_type text,
  CONSTRAINT tin_nhan_pkey PRIMARY KEY (id),
  CONSTRAINT tin_nhan_phieu_cuu_tro_id_fkey FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT tin_nhan_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id),
  CONSTRAINT tin_nhan_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.nguoi_dung(id)
);
CREATE TABLE public.tin_nhan_da_xem (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phieu_cuu_tro_id bigint NOT NULL,
  nguoi_dung_id uuid NOT NULL,
  last_seen_message_id bigint,
  last_seen_at timestamp with time zone,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tin_nhan_da_xem_pkey PRIMARY KEY (id),
  CONSTRAINT uk_tin_nhan_da_xem_phieu_nguoi_dung UNIQUE (phieu_cuu_tro_id, nguoi_dung_id),
  CONSTRAINT tin_nhan_da_xem_phieu_cuu_tro_id_fkey FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT tin_nhan_da_xem_nguoi_dung_id_fkey FOREIGN KEY (nguoi_dung_id) REFERENCES public.nguoi_dung(id),
  CONSTRAINT tin_nhan_da_xem_last_seen_message_id_fkey FOREIGN KEY (last_seen_message_id) REFERENCES public.tin_nhan(id)
);
CREATE TABLE public.tinh_nguyen_vien (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nguoi_dung_id uuid,
  thoi_gian timestamp without time zone,
  ghi_chu text,
  co_the_giup text,
  trang_thai boolean,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tinh_nguyen_vien_pkey PRIMARY KEY (id),
  CONSTRAINT tinh_nguyen_vien_nguoi_dung_id_fkey FOREIGN KEY (nguoi_dung_id) REFERENCES public.nguoi_dung(id)
);
CREATE TABLE public.trangthai_cuutro (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  code text,
  name text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT trangthai_cuutro_pkey PRIMARY KEY (id)
);
CREATE TABLE public.vat_pham (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten_vat_pham text,
  so_luong smallint,
  don_vi_id bigint,
  nhom_vat_pham_id bigint,
  tep_tin_id bigint,
  trang_thai boolean,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT vat_pham_pkey PRIMARY KEY (id),
  CONSTRAINT vat_pham_tep_tin_id_fkey FOREIGN KEY (tep_tin_id) REFERENCES public.tep_tin(id),
  CONSTRAINT vat_pham_don_vi_id_fkey FOREIGN KEY (don_vi_id) REFERENCES public.don_vi(id),
  CONSTRAINT vat_pham_nhom_vat_pham_id_fkey FOREIGN KEY (nhom_vat_pham_id) REFERENCES public.nhom_vat_pham(id)
);
CREATE TABLE public.vi_tri (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  lat text,
  long text,
  dia_chi text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT vi_tri_pkey PRIMARY KEY (id)
);
