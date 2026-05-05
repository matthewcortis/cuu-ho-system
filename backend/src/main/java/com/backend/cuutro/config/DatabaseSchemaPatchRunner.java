package com.backend.cuutro.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaPatchRunner implements CommandLineRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... args) {
		patchPhanCongTrangThaiConstraint();
	}

	private void patchPhanCongTrangThaiConstraint() {
		try {
			Boolean tableExists = jdbcTemplate.queryForObject(
					"""
							SELECT EXISTS (
								SELECT 1
								FROM information_schema.tables
								WHERE table_schema = 'public'
								  AND table_name = 'phan_cong'
							)
							""",
					Boolean.class);
			if (!Boolean.TRUE.equals(tableExists)) {
				return;
			}

			jdbcTemplate.execute(
					"""
							DO $$
							BEGIN
								IF EXISTS (
									SELECT 1
									FROM pg_constraint
									WHERE conname = 'phan_cong_trang_thai_check'
								) THEN
									ALTER TABLE public.phan_cong DROP CONSTRAINT phan_cong_trang_thai_check;
								END IF;

								ALTER TABLE public.phan_cong
								ADD CONSTRAINT phan_cong_trang_thai_check
								CHECK (
									trang_thai::text = ANY (
										ARRAY[
											'assigned'::text,
											'in_progress'::text,
											'completed'::text,
											'pending'::text,
											'accepted'::text,
											'processing'::text,
											'done'::text,
											'CHO_DIEU_PHOI'::text,
											'DA_NHAN'::text,
											'DANG_TREN_DUONG_TOI'::text,
											'DANG_XU_LY'::text,
											'HOAN_THANH'::text,
											'HUY'::text
										]
									)
								);
							END $$;
							""");
		} catch (Exception exception) {
			log.warn("Skip patch phan_cong.trang_thai constraint: {}", exception.getMessage());
		}
	}
}
