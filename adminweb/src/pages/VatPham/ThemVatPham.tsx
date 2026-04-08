import { FormEvent, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useDropzone } from "react-dropzone";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import Label from "../../components/form/Label";
import Input from "../../components/form/input/InputField";
import Select from "../../components/form/Select";
import ThemVatPhamTable, {
  type ThemVatPhamItem,
} from "../../components/tables/VatPham/ThemVatPham";
import { mockDatabase, type TepTinRecord } from "../../data/schemaMockData";

interface ThemVatPhamFormValues {
  tenVatPham: string;
  soLuong: string;
}

interface FormErrors {
  tenVatPham: string;
  soLuong: string;
  imageFile: string;
  donViId: string;
  nhomVatPhamId: string;
}

function buildDefaultItems(): ThemVatPhamItem[] {
  return mockDatabase.vat_pham.map((vatPham) => {
    const donVi = mockDatabase.don_vi.find((item) => item.id === vatPham.don_vi_id);
    const nhomVatPham = mockDatabase.nhom_vat_pham.find(
      (item) => item.id === vatPham.nhom_vat_pham_id
    );

    return {
      id: vatPham.id,
      tenVatPham: vatPham.ten_vat_pham?.trim() || "Chua dat ten",
      soLuong: vatPham.so_luong ?? 0,
      donVi: donVi?.ten || "Chua chon don vi",
      nhomVatPham: nhomVatPham?.ten || "Chua chon nhom",
      imageUrl: "",
      imagePath: "",
      tepTinId: null,
      createdAt: vatPham.created_at,
    };
  });
}

function getInitialErrors(): FormErrors {
  return {
    tenVatPham: "",
    soLuong: "",
    imageFile: "",
    donViId: "",
    nhomVatPhamId: "",
  };
}

function sanitizeFileName(fileName: string): string {
  return fileName
    .toLowerCase()
    .trim()
    .replace(/\s+/g, "-")
    .replace(/[^a-z0-9._-]/g, "");
}

function buildImageStoragePath(fileName: string): string {
  const safeFileName = sanitizeFileName(fileName || "image.jpg");
  return `/uploads/vat-pham/${Date.now()}-${safeFileName}`;
}

function detectImageMimeTypeFromFile(file: File, storagePath: string): string {
  if (file.type) return file.type;

  const normalized = storagePath.toLowerCase();
  if (normalized.endsWith(".png")) return "image/png";
  if (normalized.endsWith(".webp")) return "image/webp";
  if (normalized.endsWith(".gif")) return "image/gif";
  if (normalized.endsWith(".svg")) return "image/svg+xml";
  return "image/jpeg";
}

export default function ThemVatPhamPage() {
  const [items, setItems] = useState<ThemVatPhamItem[]>(() => buildDefaultItems());
  const [tepTinItems, setTepTinItems] = useState<TepTinRecord[]>(() => [
    ...mockDatabase.tep_tin,
  ]);
  const [formValues, setFormValues] = useState<ThemVatPhamFormValues>({
    tenVatPham: "",
    soLuong: "",
  });
  const [selectedDonViId, setSelectedDonViId] = useState("");
  const [selectedNhomVatPhamId, setSelectedNhomVatPhamId] = useState("");
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
  const [selectedImagePreviewUrl, setSelectedImagePreviewUrl] = useState("");
  const [selectedImageStoragePath, setSelectedImageStoragePath] = useState("");
  const [errors, setErrors] = useState<FormErrors>(() => getInitialErrors());
  const [selectInputKey, setSelectInputKey] = useState(0);
  const createdObjectUrlsRef = useRef<string[]>([]);

  useEffect(
    () => () => {
      createdObjectUrlsRef.current.forEach((url) => URL.revokeObjectURL(url));
      createdObjectUrlsRef.current = [];
    },
    []
  );

  const donViOptions = useMemo(
    () =>
      mockDatabase.don_vi.map((item) => ({
        value: String(item.id),
        label: item.ten || "Khong co ten",
      })),
    []
  );
  const nhomVatPhamOptions = useMemo(
    () =>
      mockDatabase.nhom_vat_pham.map((item) => ({
        value: String(item.id),
        label: item.ten || "Khong co ten",
      })),
    []
  );

  const selectedNhomVatPhamLabel =
    nhomVatPhamOptions.find((item) => item.value === selectedNhomVatPhamId)?.label ||
    "";

  const revokeObjectUrl = (url: string) => {
    if (!url || !url.startsWith("blob:")) return;
    URL.revokeObjectURL(url);
    createdObjectUrlsRef.current = createdObjectUrlsRef.current.filter(
      (currentUrl) => currentUrl !== url
    );
  };

  const clearSelectedImage = (shouldRevoke: boolean) => {
    if (shouldRevoke && selectedImagePreviewUrl) {
      revokeObjectUrl(selectedImagePreviewUrl);
    }
    setSelectedImageFile(null);
    setSelectedImagePreviewUrl("");
    setSelectedImageStoragePath("");
  };

  const handleDropImage = useCallback(
    (acceptedFiles: File[]) => {
      const file = acceptedFiles[0];
      if (!file) return;

      if (selectedImagePreviewUrl) {
        revokeObjectUrl(selectedImagePreviewUrl);
      }

      const previewUrl = URL.createObjectURL(file);
      createdObjectUrlsRef.current.push(previewUrl);

      setSelectedImageFile(file);
      setSelectedImagePreviewUrl(previewUrl);
      setSelectedImageStoragePath(buildImageStoragePath(file.name));
      setErrors((prev) => ({
        ...prev,
        imageFile: "",
      }));
    },
    [selectedImagePreviewUrl]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop: handleDropImage,
    multiple: false,
    accept: {
      "image/png": [],
      "image/jpeg": [],
      "image/webp": [],
      "image/svg+xml": [],
      "image/gif": [],
    },
  });

  const handleChangeFormValue = (
    field: keyof ThemVatPhamFormValues,
    value: string
  ) => {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
    setErrors((prev) => ({
      ...prev,
      [field]: "",
    }));
  };

  const handleSelectDonVi = (value: string) => {
    setSelectedDonViId(value);
    setErrors((prev) => ({
      ...prev,
      donViId: "",
    }));
  };

  const handleSelectNhomVatPham = (value: string) => {
    setSelectedNhomVatPhamId(value);
    setErrors((prev) => ({
      ...prev,
      nhomVatPhamId: "",
    }));
  };

  const resetForm = (options?: { revokeImage?: boolean }) => {
    setFormValues({
      tenVatPham: "",
      soLuong: "",
    });
    setSelectedDonViId("");
    setSelectedNhomVatPhamId("");
    setErrors(getInitialErrors());
    setSelectInputKey((prev) => prev + 1);
    clearSelectedImage(options?.revokeImage ?? true);
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const tenVatPham = formValues.tenVatPham.trim();
    const soLuong = Number(formValues.soLuong);

    const nextErrors = getInitialErrors();
    if (!tenVatPham) {
      nextErrors.tenVatPham = "Vui long nhap ten vat pham.";
    }
    if (!formValues.soLuong.trim() || Number.isNaN(soLuong) || soLuong <= 0) {
      nextErrors.soLuong = "So luong phai lon hon 0.";
    }
    if (!selectedImageFile || !selectedImagePreviewUrl || !selectedImageStoragePath) {
      nextErrors.imageFile = "Vui long tai anh bang Dropzone.";
    }
    if (!selectedDonViId) {
      nextErrors.donViId = "Vui long chon don vi.";
    }
    if (!selectedNhomVatPhamId) {
      nextErrors.nhomVatPhamId = "Vui long chon nhom vat pham.";
    }

    setErrors(nextErrors);
    if (
      nextErrors.tenVatPham ||
      nextErrors.soLuong ||
      nextErrors.imageFile ||
      nextErrors.donViId ||
      nextErrors.nhomVatPhamId
    ) {
      return;
    }

    const imageFile = selectedImageFile;
    const imagePreviewUrl = selectedImagePreviewUrl;
    const imageStoragePath = selectedImageStoragePath;
    if (!imageFile || !imagePreviewUrl || !imageStoragePath) {
      return;
    }

    const donViLabel =
      donViOptions.find((item) => item.value === selectedDonViId)?.label ||
      "Chua chon don vi";
    const nhomVatPhamLabel =
      nhomVatPhamOptions.find((item) => item.value === selectedNhomVatPhamId)?.label ||
      "Chua chon nhom";

    const nextTepTinId =
      tepTinItems.reduce((maxId, item) => Math.max(maxId, item.id), 0) + 1;
    const createdAt = new Date().toISOString();

    const tepTinRecord: TepTinRecord = {
      id: nextTepTinId,
      created_at: createdAt,
      duong_dan: imageStoragePath,
      loai_tep_tin: detectImageMimeTypeFromFile(imageFile, imageStoragePath),
    };

    setTepTinItems((prev) => [tepTinRecord, ...prev]);

    setItems((prev) => {
      const nextId = prev.reduce((maxId, item) => Math.max(maxId, item.id), 0) + 1;
      return [
        {
          id: nextId,
          tenVatPham,
          soLuong,
          donVi: donViLabel,
          nhomVatPham: nhomVatPhamLabel,
          imageUrl: imagePreviewUrl,
          imagePath: imageStoragePath,
          tepTinId: nextTepTinId,
          createdAt,
        },
        ...prev,
      ];
    });

    resetForm({ revokeImage: false });
  };

  const handleDeleteItem = (item: ThemVatPhamItem) => {
    setItems((prev) => prev.filter((currentItem) => currentItem.id !== item.id));
    if (item.imageUrl) {
      revokeObjectUrl(item.imageUrl);
    }
    if (item.tepTinId !== null) {
      setTepTinItems((prev) => prev.filter((tepTin) => tepTin.id !== item.tepTinId));
    }
  };

  return (
    <>
      <PageMeta
        title="Thêm Vật Phẩm"
        description="Trang thêm vật phẩm với lựa chọn đơn vị và nhóm vật phẩm."
      />
      <PageBreadcrumb pageTitle="Thêm Vật Phẩm" />
      <div className="space-y-6">
        <ComponentCard
          title="Nhập vật phẩm"
          desc="Nhập thông tin vật phẩm "
        >
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-vat-pham">Ten vat pham</Label>
                <Input
                  id="ten-vat-pham"
                  type="text"
                  placeholder="Ví dụ: Nước"
                  value={formValues.tenVatPham}
                  onChange={(event) =>
                    handleChangeFormValue("tenVatPham", event.target.value)
                  }
                  error={Boolean(errors.tenVatPham)}
                  hint={errors.tenVatPham}
                />
              </div>

              <div>
                <Label htmlFor="so-luong">So luong</Label>
                <Input
                  id="so-luong"
                  type="number"
                  min="1"
                  placeholder="Vi du: 100"
                  value={formValues.soLuong}
                  onChange={(event) =>
                    handleChangeFormValue("soLuong", event.target.value)
                  }
                  error={Boolean(errors.soLuong)}
                  hint={errors.soLuong}
                />
              </div>

              <div className="lg:col-span-2">
                <Label>Ảnh vật phẩm</Label>
                <div
                  {...getRootProps()}
                  className={`transition border border-dashed rounded-xl cursor-pointer p-6 lg:p-8 ${
                    isDragActive
                      ? "border-brand-500 bg-gray-100 dark:bg-gray-800"
                      : "border-gray-300 bg-gray-50 dark:border-gray-700 dark:bg-gray-900"
                  }`}
                >
                  <input {...getInputProps()} />
                  <div className="flex flex-col items-center text-center">
                    <div className="mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-gray-200 text-gray-700 dark:bg-gray-800 dark:text-gray-400">
                      <svg
                        className="fill-current"
                        width="24"
                        height="24"
                        viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M12 3.25C11.8 3.25 11.61 3.33 11.46 3.48L7.46 7.48C7.17 7.77 7.17 8.23 7.46 8.52C7.75 8.81 8.21 8.81 8.5 8.52L11.25 5.77V14.5C11.25 14.91 11.59 15.25 12 15.25C12.41 15.25 12.75 14.91 12.75 14.5V5.77L15.5 8.52C15.79 8.81 16.25 8.81 16.54 8.52C16.83 8.23 16.83 7.77 16.54 7.48L12.54 3.48C12.39 3.33 12.2 3.25 12 3.25Z" />
                        <path d="M5 15.25C5.41 15.25 5.75 15.59 5.75 16V18C5.75 18.41 6.09 18.75 6.5 18.75H17.5C17.91 18.75 18.25 18.41 18.25 18V16C18.25 15.59 18.59 15.25 19 15.25C19.41 15.25 19.75 15.59 19.75 16V18C19.75 19.24 18.74 20.25 17.5 20.25H6.5C5.26 20.25 4.25 19.24 4.25 18V16C4.25 15.59 4.59 15.25 5 15.25Z" />
                      </svg>
                    </div>
                    <p className="text-theme-sm font-medium text-gray-800 dark:text-white/90">
                      {isDragActive ? "Drop file anh vao day" : "Keo tha anh vao day"}
                    </p>
                    <p className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                      Hoac bam de chon file PNG, JPG, WebP, SVG, GIF
                    </p>
                  </div>
                </div>

                {selectedImagePreviewUrl && (
                  <div className="mt-3 flex items-start gap-3 rounded-xl border border-gray-200 bg-gray-50 p-3 dark:border-white/[0.08] dark:bg-white/[0.03]">
                    <img
                      src={selectedImagePreviewUrl}
                      alt="Preview"
                      className="h-16 w-16 rounded-lg object-cover"
                    />
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium text-gray-800 text-theme-sm dark:text-white/90">
                        {selectedImageFile?.name}
                      </p>
                      <p className="mt-1 break-all text-theme-xs text-gray-500 dark:text-gray-400">
                        Đường dẫn lưu: {selectedImageStoragePath}
                      </p>
                      <button
                        type="button"
                        onClick={() => clearSelectedImage(true)}
                        className="mt-2 inline-flex items-center rounded-lg border border-gray-300 px-3 py-1.5 text-theme-xs font-medium text-gray-700 hover:bg-gray-100 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                      >
                        Bỏ ảnh đã chọn
                      </button>
                    </div>
                  </div>
                )}

                {errors.imageFile && (
                  <p className="mt-1.5 text-xs text-error-500">{errors.imageFile}</p>
                )}
              </div>

              <div>
                <Label>Chọn đơn vị</Label>
                <Select
                  key={`don-vi-${selectInputKey}`}
                  options={donViOptions}
                  placeholder="Chọn đơn vị"
                  onChange={handleSelectDonVi}
                  className="dark:bg-dark-900"
                />
                {errors.donViId && (
                  <p className="mt-1.5 text-xs text-error-500">{errors.donViId}</p>
                )}
              </div>

              <div>
                <Label>Thêm vào nhóm vật phẩm</Label>
                <Select
                  key={`nhom-vat-pham-${selectInputKey}`}
                  options={nhomVatPhamOptions}
                  placeholder="Chọn nhóm vật phẩm"
                  onChange={handleSelectNhomVatPham}
                  className="dark:bg-dark-900"
                />
                {errors.nhomVatPhamId && (
                  <p className="mt-1.5 text-xs text-error-500">{errors.nhomVatPhamId}</p>
                )}
              </div>
            </div>

            {selectedNhomVatPhamLabel && (
              <p className="text-theme-sm text-gray-600 dark:text-gray-300">
                Vật phẩm sẽ được thêm vào nhóm:
                <span className="ml-1 font-medium text-brand-600 dark:text-brand-400">
                  {selectedNhomVatPhamLabel}
                </span>
              </p>
            )}

            <p className="text-theme-xs text-gray-500 dark:text-gray-400">
              Bản ghi tệp tin hiện tại: {tepTinItems.length}
            </p>

            <div className="flex flex-wrap items-center gap-3">
              <button
                type="submit"
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
              >
                Thêm vật phẩm
              </button>

              <button
                type="button"
                onClick={() => resetForm({ revokeImage: true })}
                className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
              >
                Đặt lại
              </button>
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sách vật phẩm đã thêm">
          <ThemVatPhamTable items={items} onDeleteItem={handleDeleteItem} />
        </ComponentCard>
      </div>
    </>
  );
}
