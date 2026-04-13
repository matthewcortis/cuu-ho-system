import { FormEvent, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useDropzone } from "react-dropzone";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import Select from "@/components/form/Select";
import ThemVatPhamTable, {
  type ThemVatPhamItem,
} from "@/features/vat-pham/components/ThemVatPham";
import {
  createVatPhamWithImage,
  deleteVatPham,
  fetchVatPhamList,
  updateVatPham,
  type VatPhamDto,
  VatPhamApiError,
} from "@/features/vat-pham/api/vatPhamApi";
import { fetchDonViList } from "@/features/vat-pham/api/donViApi";
import { fetchNhomVatPhamList } from "@/features/vat-pham/api/nhomVatPhamApi";

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

interface SelectOption {
  value: string;
  label: string;
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

function mapVatPhamDtoToItem(vatPham: VatPhamDto): ThemVatPhamItem {
  const imageUrl = vatPham.tepTin?.duongDan?.trim() || "";

  return {
    id: vatPham.id,
    tenVatPham: vatPham.tenVatPham?.trim() || "Chua dat ten",
    soLuong: Number.isFinite(vatPham.soLuong) ? vatPham.soLuong : 0,
    donVi: vatPham.donVi?.ten?.trim() || "Chua chon don vi",
    donViId: vatPham.donVi?.id ?? null,
    nhomVatPham: vatPham.nhomVatPham?.ten?.trim() || "Chua chon nhom",
    nhomVatPhamId: vatPham.nhomVatPham?.id ?? null,
    imageUrl,
    imagePath: imageUrl,
    tepTinId: vatPham.tepTin?.id ?? null,
    createdAt: vatPham.createdAt,
  };
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof VatPhamApiError) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the xu ly yeu cau vat pham. Vui long thu lai.";
}

export default function ThemVatPhamPage() {
  const [items, setItems] = useState<ThemVatPhamItem[]>([]);
  const [formValues, setFormValues] = useState<ThemVatPhamFormValues>({
    tenVatPham: "",
    soLuong: "",
  });
  const [donViOptions, setDonViOptions] = useState<SelectOption[]>([]);
  const [nhomVatPhamOptions, setNhomVatPhamOptions] = useState<SelectOption[]>([]);
  const [selectedDonViId, setSelectedDonViId] = useState("");
  const [selectedNhomVatPhamId, setSelectedNhomVatPhamId] = useState("");
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
  const [selectedImagePreviewUrl, setSelectedImagePreviewUrl] = useState("");
  const [selectedImageStoragePath, setSelectedImageStoragePath] = useState("");
  const [errors, setErrors] = useState<FormErrors>(() => getInitialErrors());
  const [selectInputKey, setSelectInputKey] = useState(0);
  const [editingItemId, setEditingItemId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const createdObjectUrlsRef = useRef<string[]>([]);

  useEffect(
    () => () => {
      createdObjectUrlsRef.current.forEach((url) => URL.revokeObjectURL(url));
      createdObjectUrlsRef.current = [];
    },
    []
  );

  useEffect(() => {
    let isMounted = true;

    const loadData = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const [vatPhamList, donViList, nhomVatPhamList] = await Promise.all([
          fetchVatPhamList(),
          fetchDonViList(),
          fetchNhomVatPhamList(),
        ]);

        if (!isMounted) {
          return;
        }

        setItems(vatPhamList.map(mapVatPhamDtoToItem));
        setDonViOptions(
          donViList.map((item) => ({
            value: String(item.id),
            label: item.ten?.trim() || "Khong co ten",
          }))
        );
        setNhomVatPhamOptions(
          nhomVatPhamList.map((item) => ({
            value: String(item.id),
            label: item.ten?.trim() || "Khong co ten",
          }))
        );
      } catch (error) {
        if (!isMounted) {
          return;
        }
        setLoadError(getApiErrorMessage(error));
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void loadData();

    return () => {
      isMounted = false;
    };
  }, []);

  const selectedNhomVatPhamLabel =
    nhomVatPhamOptions.find((item) => item.value === selectedNhomVatPhamId)?.label ||
    "";

  const tepTinCount = useMemo(
    () => items.filter((item) => item.tepTinId !== null).length,
    [items]
  );
  const editingItem = useMemo(
    () => items.find((item) => item.id === editingItemId) ?? null,
    [editingItemId, items]
  );

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
      setSelectedImageStoragePath(file.name);
      setErrors((prev) => ({
        ...prev,
        imageFile: "",
      }));
      setActionError(null);
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
    setActionError(null);
  };

  const handleSelectDonVi = (value: string) => {
    setSelectedDonViId(value);
    setErrors((prev) => ({
      ...prev,
      donViId: "",
    }));
    setActionError(null);
  };

  const handleSelectNhomVatPham = (value: string) => {
    setSelectedNhomVatPhamId(value);
    setErrors((prev) => ({
      ...prev,
      nhomVatPhamId: "",
    }));
    setActionError(null);
  };

  const resetForm = (options?: { revokeImage?: boolean }) => {
    setFormValues({
      tenVatPham: "",
      soLuong: "",
    });
    setEditingItemId(null);
    setSelectedDonViId("");
    setSelectedNhomVatPhamId("");
    setErrors(getInitialErrors());
    setSelectInputKey((prev) => prev + 1);
    clearSelectedImage(options?.revokeImage ?? true);
    setActionError(null);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isSubmitting) {
      return;
    }

    const tenVatPham = formValues.tenVatPham.trim();
    const soLuong = Number(formValues.soLuong);

    const nextErrors = getInitialErrors();
    if (!tenVatPham) {
      nextErrors.tenVatPham = "Vui long nhap ten vat pham.";
    }
    if (!formValues.soLuong.trim() || Number.isNaN(soLuong) || soLuong <= 0) {
      nextErrors.soLuong = "So luong phai lon hon 0.";
    }
    if (
      editingItemId === null &&
      (!selectedImageFile || !selectedImagePreviewUrl || !selectedImageStoragePath)
    ) {
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

    setIsSubmitting(true);
    setActionError(null);

    try {
      if (editingItemId !== null) {
        const updated = await updateVatPham(editingItemId, {
          tenVatPham,
          soLuong,
          donViId: Number(selectedDonViId),
          nhomVatPhamId: Number(selectedNhomVatPhamId),
          tepTinId: editingItem?.tepTinId ?? null,
          trangThai: true,
        });
        const updatedItem = mapVatPhamDtoToItem(updated);
        setItems((prev) =>
          prev.map((item) =>
            item.id === editingItemId
              ? {
                  ...item,
                  ...updatedItem,
                }
              : item
          )
        );
        resetForm({ revokeImage: true });
        return;
      }

      const imageFile = selectedImageFile;
      if (!imageFile) {
        return;
      }

      const created = await createVatPhamWithImage({
        tenVatPham,
        soLuong,
        donViId: Number(selectedDonViId),
        nhomVatPhamId: Number(selectedNhomVatPhamId),
        anhVatPham: imageFile,
        trangThai: true,
      });

      const createdItem = mapVatPhamDtoToItem(created);
      setItems((prev) => [createdItem, ...prev.filter((item) => item.id !== createdItem.id)]);
      resetForm({ revokeImage: true });
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEditItem = (item: ThemVatPhamItem) => {
    if (isSubmitting) {
      return;
    }
    if (item.donViId === null || item.nhomVatPhamId === null) {
      setActionError("Khong the chinh sua vat pham nay vi thieu don vi hoac nhom vat pham.");
      return;
    }

    clearSelectedImage(true);
    setEditingItemId(item.id);
    setFormValues({
      tenVatPham: item.tenVatPham,
      soLuong: String(item.soLuong),
    });
    setSelectedDonViId(String(item.donViId));
    setSelectedNhomVatPhamId(String(item.nhomVatPhamId));
    setErrors(getInitialErrors());
    setSelectInputKey((prev) => prev + 1);
    setActionError(null);
  };

  const handleDeleteItem = async (item: ThemVatPhamItem) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      await deleteVatPham(item.id);
      setItems((prev) => prev.filter((currentItem) => currentItem.id !== item.id));
      if (item.imageUrl) {
        revokeObjectUrl(item.imageUrl);
      }
      if (editingItemId === item.id) {
        resetForm({ revokeImage: true });
      }
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <PageMeta
        title="Them Vat Pham"
        description="Trang them vat pham voi lua chon don vi va nhom vat pham."
      />
      <PageBreadcrumb pageTitle="Them Vat Pham" />

      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Khong the tai du lieu vat pham: {loadError}
          </div>
        )}

        {actionError && (
          <div className="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-theme-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-300">
            {actionError}
          </div>
        )}

        <ComponentCard
          title={editingItemId !== null ? "Chinh sua vat pham" : "Nhap vat pham"}
          desc={
            editingItemId !== null
              ? "Cap nhat thong tin vat pham. Anh hien tai duoc giu nguyen."
              : "Nhap thong tin vat pham"
          }
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Dang tai danh sach vat pham, don vi va nhom vat pham...
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-vat-pham">Ten vat pham</Label>
                <Input
                  id="ten-vat-pham"
                  type="text"
                  placeholder="Vi du: Nuoc"
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

              {editingItemId === null ? (
                <div className="lg:col-span-2">
                  <Label>Anh vat pham</Label>
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
                          Tep tai len: {selectedImageStoragePath}
                        </p>
                        <button
                          type="button"
                          onClick={() => clearSelectedImage(true)}
                          className="mt-2 inline-flex items-center rounded-lg border border-gray-300 px-3 py-1.5 text-theme-xs font-medium text-gray-700 hover:bg-gray-100 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                        >
                          Bo anh da chon
                        </button>
                      </div>
                    </div>
                  )}

                  {errors.imageFile && (
                    <p className="mt-1.5 text-xs text-error-500">{errors.imageFile}</p>
                  )}
                </div>
              ) : (
                <div className="lg:col-span-2 rounded-xl border border-gray-200 bg-gray-50 p-4 text-theme-sm text-gray-600 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-300">
                  Che do chinh sua chi cap nhat thong tin vat pham. Anh hien tai se duoc giu nguyen.
                </div>
              )}

              <div>
                <Label>Chon don vi</Label>
                <Select
                  key={`don-vi-${selectInputKey}`}
                  defaultValue={selectedDonViId}
                  options={donViOptions}
                  placeholder="Chon don vi"
                  onChange={handleSelectDonVi}
                  className="dark:bg-dark-900"
                />
                {errors.donViId && (
                  <p className="mt-1.5 text-xs text-error-500">{errors.donViId}</p>
                )}
              </div>

              <div>
                <Label>Them vao nhom vat pham</Label>
                <Select
                  key={`nhom-vat-pham-${selectInputKey}`}
                  defaultValue={selectedNhomVatPhamId}
                  options={nhomVatPhamOptions}
                  placeholder="Chon nhom vat pham"
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
                Vat pham se duoc them vao nhom:
                <span className="ml-1 font-medium text-brand-600 dark:text-brand-400">
                  {selectedNhomVatPhamLabel}
                </span>
              </p>
            )}

            <p className="text-theme-xs text-gray-500 dark:text-gray-400">
              Ban ghi tep tin hien tai: {tepTinCount}
            </p>

            <div className="flex flex-wrap items-center gap-3">
              <button
                type="submit"
                disabled={isSubmitting}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isSubmitting
                  ? "Dang xu ly..."
                  : editingItemId !== null
                    ? "Cap nhat vat pham"
                    : "Them vat pham"}
              </button>

              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => resetForm({ revokeImage: true })}
                className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
              >
                Dat lai
              </button>

              {editingItemId !== null && (
                <button
                  type="button"
                  disabled={isSubmitting}
                  onClick={() => resetForm({ revokeImage: true })}
                  className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                >
                  Huy chinh sua
                </button>
              )}
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sach vat pham da them">
          <ThemVatPhamTable
            items={items}
            onEditItem={handleEditItem}
            onDeleteItem={(item) => {
              void handleDeleteItem(item);
            }}
          />
        </ComponentCard>
      </div>
    </>
  );
}
