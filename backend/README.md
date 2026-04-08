-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.bang_tin (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  tieu_de character varying,
  noi_dung character varying,
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
CREATE TABLE public.chi_tiet_cuu_tro (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phieu_cuu_tro_id bigint,
  vat_pham_id bigint,
  so_luong integer,
  ghi_chu character varying,
  created_at timestamp without time zone DEFAULT now(),
  CONSTRAINT chi_tiet_cuu_tro_pkey PRIMARY KEY (id),
  CONSTRAINT fk_phieu FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT fk_vat_pham FOREIGN KEY (vat_pham_id) REFERENCES public.vat_pham(id)
);
CREATE TABLE public.doi_nhom (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten_doi_nhom character varying,
  so_dien_thoai character varying,
  vi_tri_id bigint,
  trang_thai_hoat_dong boolean DEFAULT true,
  active boolean DEFAULT true,
  trang_thai character varying DEFAULT 'idle'::text CHECK (trang_thai::text = ANY (ARRAY['idle'::text, 'busy'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT doi_nhom_pkey PRIMARY KEY (id),
  CONSTRAINT doi_nhom_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id)
);
CREATE TABLE public.doi_nhom_tinh_nguyen_vien (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  doi_nhom_id bigint,
  tinh_nguyen_vien_id bigint,
  vai_tro character varying CHECK (vai_tro::text = ANY (ARRAY['truong_nhom'::text, 'pho_nhom'::text, 'thanh_vien'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT doi_nhom_tinh_nguyen_vien_pkey PRIMARY KEY (id),
  CONSTRAINT doi_nhom_tinh_nguyen_vien_doi_nhom_id_fkey FOREIGN KEY (doi_nhom_id) REFERENCES public.doi_nhom(id),
  CONSTRAINT doi_nhom_tinh_nguyen_vien_tinh_nguyen_vien_id_fkey FOREIGN KEY (tinh_nguyen_vien_id) REFERENCES public.tinh_nguyen_vien(id)
);
CREATE TABLE public.don_vi (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten character varying,
  ma_don_vi character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT don_vi_pkey PRIMARY KEY (id)
);
CREATE TABLE public.khai_bao (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT khai_bao_pkey PRIMARY KEY (id)
);
CREATE TABLE public.loai_su_co (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten character varying,
  icon_url character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT loai_su_co_pkey PRIMARY KEY (id)
);
CREATE TABLE public.nguoi_dung (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  tai_khoan_id bigint,
  ten character varying,
  sdt character varying,
  vi_tri_id bigint,
  avatar_url character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT nguoi_dung_pkey PRIMARY KEY (id),
  CONSTRAINT nguoi_dung_tai_khoan_id_fkey FOREIGN KEY (tai_khoan_id) REFERENCES public.tai_khoan(id),
  CONSTRAINT nguoi_dung_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id)
);
CREATE TABLE public.nguoi_dung_phan_quyen (
  nguoi_dung_id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phan_quyen_id bigint NOT NULL,
  CONSTRAINT nguoi_dung_phan_quyen_pkey PRIMARY KEY (nguoi_dung_id),
  CONSTRAINT fkiw3y6e70vom8wvipc8dtkh4mk FOREIGN KEY (phan_quyen_id) REFERENCES public.phan_quyen(id)
);
CREATE TABLE public.nhom_vat_pham (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten character varying,
  mo_ta character varying,
  created_at timestamp with time zone DEFAULT now(),
  loai_su_co_id bigint,
  CONSTRAINT nhom_vat_pham_pkey PRIMARY KEY (id),
  CONSTRAINT nhom_vat_pham_danh_sach_cuu_tro_id_fkey FOREIGN KEY (loai_su_co_id) REFERENCES public.loai_su_co(id)
);
CREATE TABLE public.phan_cong (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phieu_cuu_tro_id bigint,
  doi_nhom_id bigint,
  assigned_at timestamp with time zone DEFAULT now(),
  trang_thai character varying DEFAULT 'assigned'::text CHECK (trang_thai::text = ANY (ARRAY['assigned'::text, 'in_progress'::text, 'completed'::text])),
  CONSTRAINT phan_cong_pkey PRIMARY KEY (id),
  CONSTRAINT phan_cong_phieu_cuu_tro_id_fkey FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT phan_cong_doi_nhom_id_fkey FOREIGN KEY (doi_nhom_id) REFERENCES public.doi_nhom(id)
);
CREATE TABLE public.phan_quyen (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  mo_ta character varying,
  ten character varying,
  CONSTRAINT phan_quyen_pkey PRIMARY KEY (id)
);
CREATE TABLE public.phieu_cuu_tro (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  loai_su_co_id bigint,
  vi_tri_id bigint,
  tep_tin_id bigint,
  nguoi_dung_id uuid,
  ho_ten character varying,
  sdt character varying,
  ghi_chu character varying,
  trang_thai character varying DEFAULT 'pending'::text CHECK (trang_thai::text = ANY (ARRAY['pending'::text, 'assigned'::text, 'processing'::text, 'done'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT phieu_cuu_tro_pkey PRIMARY KEY (id),
  CONSTRAINT phieu_cuu_tro_danh_sach_cuu_tro_id_fkey FOREIGN KEY (loai_su_co_id) REFERENCES public.loai_su_co(id),
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
  CONSTRAINT so_luong_vat_pham_khai_bao_id_fkey FOREIGN KEY (khai_bao_id) REFERENCES public.khai_bao(id),
  CONSTRAINT fk9cb0d8xp1mcm1teosgas06118 FOREIGN KEY (vat_pham_id) REFERENCES public.vat_pham(id)
);
CREATE TABLE public.tai_khoan (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  email character varying,
  ten_dang_nhap character varying,
  mat_khau character varying,
  trang_thai boolean,
  vai_tro character varying DEFAULT 'USER'::text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tai_khoan_pkey PRIMARY KEY (id)
);
CREATE TABLE public.tep_tin (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  duong_dan character varying,
  loai_tep_tin character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tep_tin_pkey PRIMARY KEY (id)
);
CREATE TABLE public.tin_nhan (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phieu_cuu_tro_id bigint,
  sender_id uuid,
  noi_dung character varying,
  created_at timestamp with time zone DEFAULT now(),
  loai_tin_nhan character varying DEFAULT 'text'::text,
  media_url character varying,
  media_type character varying,
  CONSTRAINT tin_nhan_pkey PRIMARY KEY (id),
  CONSTRAINT tin_nhan_phieu_cuu_tro_id_fkey FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT tin_nhan_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.nguoi_dung(id)
);
CREATE TABLE public.tinh_nguyen_vien (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nguoi_dung_id uuid,
  thoi_gian timestamp without time zone,
  ghi_chu character varying,
  co_the_giup character varying,
  created_at timestamp with time zone DEFAULT now(),
  trang_thai_duyet character varying DEFAULT 'CHO_XET_DUYET'::text CHECK (trang_thai_duyet::text = ANY (ARRAY['CHO_XET_DUYET'::text, 'DUOC_DUYET'::text, 'HUY'::text])),
  thoi_gian_duyet timestamp without time zone,
  CONSTRAINT tinh_nguyen_vien_pkey PRIMARY KEY (id),
  CONSTRAINT tinh_nguyen_vien_nguoi_dung_id_fkey FOREIGN KEY (nguoi_dung_id) REFERENCES public.nguoi_dung(id)
);
CREATE TABLE public.trangthai_cuutro (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  code character varying,
  name character varying,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT trangthai_cuutro_pkey PRIMARY KEY (id)
);
CREATE TABLE public.vat_pham (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten_vat_pham character varying,
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
  lat character varying,
  long character varying,
  dia_chi character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT vi_tri_pkey PRIMARY KEY (id)
);