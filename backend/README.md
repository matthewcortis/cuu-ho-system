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
  doi_truong_id bigint,
  CONSTRAINT doi_nhom_pkey PRIMARY KEY (id),
  CONSTRAINT doi_nhom_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id),
  CONSTRAINT fkb4tecsjetflce5178sedim52j FOREIGN KEY (doi_truong_id) REFERENCES public.doi_nhom_thanh_vien(id),
  CONSTRAINT fkgtkwj9bn0m76mc3ardkoitheo FOREIGN KEY (vi_tri_id) REFERENCES public.loai_su_co(id)
);
CREATE TABLE public.doi_nhom_thanh_vien (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  so_dien_thoai character varying,
  ten_thanh_vien character varying,
  doi_nhom_id bigint,
  vi_tri_id bigint,
  tinh_nguyen_vien_id bigint,
  created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT doi_nhom_thanh_vien_pkey PRIMARY KEY (id),
  CONSTRAINT fkaqe7wq8t5ve6h2o2e7xds06u7 FOREIGN KEY (doi_nhom_id) REFERENCES public.doi_nhom(id),
  CONSTRAINT fk1gsdlhan3l1lrkn5fuc2q72x1 FOREIGN KEY (vi_tri_id) REFERENCES public.loai_su_co(id),
  CONSTRAINT fkhqv6s33ujuk2i8wohsfny3jvj FOREIGN KEY (tinh_nguyen_vien_id) REFERENCES public.tinh_nguyen_vien(id)
);
CREATE TABLE public.doi_nhom_tinh_nguyen_vien (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  doi_nhom_id bigint NOT NULL,
  tinh_nguyen_vien_id bigint NOT NULL,
  vai_tro character varying NOT NULL CHECK (vai_tro::text = ANY (ARRAY['truong_nhom'::text, 'pho_nhom'::text, 'thanh_vien'::text])),
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
  created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT khai_bao_pkey PRIMARY KEY (id)
);
CREATE TABLE public.loai_su_co (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  ten character varying,
  icon_url character varying,
  created_at timestamp with time zone DEFAULT now(),
  trang_thai boolean,
  mo_ta character varying,
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
  ma_don_vi character varying,
  CONSTRAINT nhom_vat_pham_pkey PRIMARY KEY (id),
  CONSTRAINT nhom_vat_pham_danh_sach_cuu_tro_id_fkey FOREIGN KEY (loai_su_co_id) REFERENCES public.loai_su_co(id),
  CONSTRAINT fk2jta81hpxkr8bc1pugdcfp73i FOREIGN KEY (loai_su_co_id) REFERENCES public.nhom_vat_pham(id)
);
CREATE TABLE public.nhom_vat_pham_loai_su_co (
  nhom_vat_pham_id bigint NOT NULL,
  loai_su_co_id bigint NOT NULL,
  CONSTRAINT nhom_vat_pham_loai_su_co_pkey PRIMARY KEY (nhom_vat_pham_id, loai_su_co_id),
  CONSTRAINT fkp13eq8ln3p3hslcc2lsltc7up FOREIGN KEY (loai_su_co_id) REFERENCES public.loai_su_co(id),
  CONSTRAINT fk6ovf8gjpgom664bryoc3j7svh FOREIGN KEY (nhom_vat_pham_id) REFERENCES public.nhom_vat_pham(id)
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
  nguoi_dung_id uuid,
  ho_ten character varying,
  sdt character varying,
  ghi_chu character varying,
  trang_thai character varying DEFAULT 'pending'::text CHECK (trang_thai::text = ANY (ARRAY['pending'::text, 'assigned'::text, 'processing'::text, 'done'::text])),
  created_at timestamp with time zone DEFAULT now(),
  tep_tin_id bigint,
  CONSTRAINT phieu_cuu_tro_pkey PRIMARY KEY (id),
  CONSTRAINT phieu_cuu_tro_danh_sach_cuu_tro_id_fkey FOREIGN KEY (loai_su_co_id) REFERENCES public.loai_su_co(id),
  CONSTRAINT phieu_cuu_tro_vi_tri_id_fkey FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id),
  CONSTRAINT phieu_cuu_tro_nguoi_dung_id_fkey FOREIGN KEY (nguoi_dung_id) REFERENCES public.nguoi_dung(id),
  CONSTRAINT fkicf3v4glfljlw3y2qbnbpnfcp FOREIGN KEY (tep_tin_id) REFERENCES public.tep_tin(id)
);
CREATE TABLE public.phieu_cuu_tro_tep_tin (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  phieu_cuu_tro_id bigint NOT NULL,
  tep_tin_id bigint NOT NULL,
  loai character varying NOT NULL,
  thu_tu integer DEFAULT 0,
  mo_ta character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT phieu_cuu_tro_tep_tin_pkey PRIMARY KEY (id),
  CONSTRAINT fk_pctt_phieu FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT fk_pctt_tep_tin FOREIGN KEY (tep_tin_id) REFERENCES public.tep_tin(id)
);
CREATE TABLE public.so_luong_vat_pham (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  so_luong smallint,
  khai_bao_id bigint,
  vat_pham_id bigint,
  CONSTRAINT so_luong_vat_pham_pkey PRIMARY KEY (id),
  CONSTRAINT fkm75hrexeh72u2qn3ryfldoca0 FOREIGN KEY (khai_bao_id) REFERENCES public.khai_bao(id),
  CONSTRAINT fk9cb0d8xp1mcm1teosgas06118 FOREIGN KEY (vat_pham_id) REFERENCES public.vat_pham(id)
);
CREATE TABLE public.tai_khoan (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  email character varying,
  ten_dang_nhap character varying,
  mat_khau character varying,
  trang_thai boolean,
  vai_tro character varying DEFAULT 'USER'::text CHECK (vai_tro::text = ANY (ARRAY['ADMIN'::text, 'USER'::text, 'TRUONG_NHOM_TNV'::text])),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tai_khoan_pkey PRIMARY KEY (id)
);
-- Neu DB da ton tai du lieu cu:
-- UPDATE public.tai_khoan SET vai_tro = 'TRUONG_NHOM_TNV' WHERE vai_tro = 'VOLUNTEER';
-- ALTER TABLE public.tai_khoan DROP CONSTRAINT IF EXISTS tai_khoan_vai_tro_check;
-- ALTER TABLE public.tai_khoan
--   ADD CONSTRAINT tai_khoan_vai_tro_check
--   CHECK (vai_tro = ANY (ARRAY['ADMIN', 'USER', 'TRUONG_NHOM_TNV']));
CREATE TABLE public.tep_tin (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  duong_dan character varying,
  loai_tep_tin character varying,
  created_at timestamp with time zone DEFAULT now(),
  ten_tep_tin character varying,
  kich_thuoc bigint,
  creat bigint,
  create_at bigint,
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
  vi_tri_id bigint,
  CONSTRAINT tin_nhan_pkey PRIMARY KEY (id),
  CONSTRAINT tin_nhan_phieu_cuu_tro_id_fkey FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id),
  CONSTRAINT tin_nhan_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.nguoi_dung(id),
  CONSTRAINT fkii3p3v16xm4m2k8kdv2vr89sr FOREIGN KEY (vi_tri_id) REFERENCES public.vi_tri(id)
);
CREATE TABLE public.tin_nhan_da_xem (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  last_seen_at timestamp with time zone,
  updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  last_seen_message_id bigint,
  nguoi_dung_id uuid NOT NULL,
  phieu_cuu_tro_id bigint NOT NULL,
  CONSTRAINT tin_nhan_da_xem_pkey PRIMARY KEY (id),
  CONSTRAINT fk9ynegcah00mo4eyc7od75gm5i FOREIGN KEY (last_seen_message_id) REFERENCES public.tin_nhan(id),
  CONSTRAINT fkrm0jygv6q20t5amcw75i7jxag FOREIGN KEY (nguoi_dung_id) REFERENCES public.nguoi_dung(id),
  CONSTRAINT fk78inn78inut492uh91ivmt9jf FOREIGN KEY (phieu_cuu_tro_id) REFERENCES public.phieu_cuu_tro(id)
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
  created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  name character varying,
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
CREATE TABLE public.vat_pham_nhom_vat_pham (
  vat_pham_id bigint NOT NULL,
  nhom_vat_pham_id bigint NOT NULL,
  CONSTRAINT vat_pham_nhom_vat_pham_pkey PRIMARY KEY (vat_pham_id, nhom_vat_pham_id),
  CONSTRAINT fkspy56itgc3iktaulluhsfnie9 FOREIGN KEY (nhom_vat_pham_id) REFERENCES public.nhom_vat_pham(id),
  CONSTRAINT fk60my5myif583cpbmcjwr4ft7s FOREIGN KEY (vat_pham_id) REFERENCES public.vat_pham(id)
);
CREATE TABLE public.vi_tri (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  lat character varying,
  long character varying,
  dia_chi character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT vi_tri_pkey PRIMARY KEY (id)
);
