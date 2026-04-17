import { FormEvent, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useDropzone } from "react-dropzone";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import Pagination from "@/components/common/Pagination";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import Combobox, { type ComboboxOptionItem } from "@/components/form/Combobox";
import ThemVatPhamTable, {
  type ThemVatPhamItem,
} from "@/features/vat-pham/components/ThemVatPham";
import ActionToast, {
  type ActionToastData,
  type ActionToastType,
} from "@/components/common/ActionToast";
import {
  createVatPhamWithImage,
  deleteVatPham,
  fetchVatPhamList,
  uploadVatPhamImage,
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

const MAX_SHORT = 32767;
const ROWS_PER_PAGE_OPTIONS = [10, 20, 50] as const;

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
    tenVatPham: vatPham.tenVatPham?.trim() || "Chưa đặt tên",
    soLuong: Number.isFinite(vatPham.soLuong) ? vatPham.soLuong : 0,
    donVi: vatPham.donVi?.ten?.trim() || "Chưa chọn đơn vị",
    donViId: vatPham.donVi?.id ?? null,
    nhomVatPham: vatPham.nhomVatPham?.ten?.trim() || "Chưa chọn nhóm",
    nhomVatPhamId: vatPham.nhomVatPham?.id ?? null,
    imageUrl,
    imagePath: imageUrl,
    tepTinId: vatPham.tepTin?.id ?? null,
    trangThai: vatPham.trangThai,
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

  return "Không thể xử lý yêu cầu vật phẩm. Vui lòng thử lại.";
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
  const [editingItemId, setEditingItemId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionToast, setActionToast] = useState<ActionToastData | null>(null);
  const [rowsPerPage, setRowsPerPage] = useState<number>(ROWS_PER_PAGE_OPTIONS[0]);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const createdObjectUrlsRef = useRef<string[]>([]);
  const actionToastIdRef = useRef(0);

  const showActionToast = useCallback(
    (type: ActionToastType, title: string, message: string) => {
      actionToastIdRef.current += 1;
      setActionToast({
        id: actionToastIdRef.current,
        type,
        title,
        message,
      });
    },
    []
  );

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
        const [vatPhamResult, donViResult, nhomVatPhamResult] = await Promise.allSettled([
          fetchVatPhamList(),
          fetchDonViList(),
          fetchNhomVatPhamList(),
        ]);

        if (!isMounted) {
          return;
        }

        const loadErrors: string[] = [];

        if (vatPhamResult.status === "fulfilled") {
          setItems(vatPhamResult.value.map(mapVatPhamDtoToItem));
        } else {
          loadErrors.push(`Vật phẩm: ${getApiErrorMessage(vatPhamResult.reason)}`);
        }

        if (donViResult.status === "fulfilled") {
          setDonViOptions(
            donViResult.value.map((item) => ({
              value: String(item.id),
              label: item.ten?.trim() || "Không có tên",
            }))
          );
        } else {
          loadErrors.push(`Đơn vị: ${getApiErrorMessage(donViResult.reason)}`);
        }

        if (nhomVatPhamResult.status === "fulfilled") {
          setNhomVatPhamOptions(
            nhomVatPhamResult.value.map((item) => ({
              value: String(item.id),
              label: item.ten?.trim() || "Không có tên",
            }))
          );
        } else {
          loadErrors.push(`Nhóm vật phẩm: ${getApiErrorMessage(nhomVatPhamResult.reason)}`);
        }

        setLoadError(loadErrors.length > 0 ? loadErrors.join(" | ") : null);
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
  const totalItems = items.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / rowsPerPage));
  const donViComboboxOptions = useMemo<ComboboxOptionItem[]>(
    () =>
      donViOptions.map((option) => ({
        value: option.value,
        label: option.label,
        searchText: option.label,
      })),
    [donViOptions]
  );
  const nhomVatPhamComboboxOptions = useMemo<ComboboxOptionItem[]>(
    () =>
      nhomVatPhamOptions.map((option) => ({
        value: option.value,
        label: option.label,
        searchText: option.label,
      })),
    [nhomVatPhamOptions]
  );
  const paginatedItems = useMemo(() => {
    const startIndex = (currentPage - 1) * rowsPerPage;
    return items.slice(startIndex, startIndex + rowsPerPage);
  }, [currentPage, items, rowsPerPage]);

  useEffect(() => {
    setCurrentPage(1);
  }, [rowsPerPage]);

  useEffect(() => {
    setCurrentPage((prev) => Math.min(prev, totalPages));
  }, [totalPages]);

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
    setEditingItemId(null);
    setSelectedDonViId("");
    setSelectedNhomVatPhamId("");
    setErrors(getInitialErrors());
    clearSelectedImage(options?.revokeImage ?? true);
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
      nextErrors.tenVatPham = "Vui lòng nhập tên vật phẩm.";
    }
    if (
      !formValues.soLuong.trim() ||
      Number.isNaN(soLuong) ||
      !Number.isInteger(soLuong) ||
      soLuong <= 0 ||
      soLuong > MAX_SHORT
    ) {
      nextErrors.soLuong = `Số lượng phải trong khoảng 1 đến ${MAX_SHORT}.`;
    }
    if (
      editingItemId === null &&
      (!selectedImageFile || !selectedImagePreviewUrl || !selectedImageStoragePath)
    ) {
      nextErrors.imageFile = "Vui lòng tải ảnh bằng Dropzone.";
    }
    if (!selectedDonViId) {
      nextErrors.donViId = "Vui lòng chọn đơn vị.";
    }
    if (!selectedNhomVatPhamId) {
      nextErrors.nhomVatPhamId = "Vui lòng chọn nhóm vật phẩm.";
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

    try {
      if (editingItemId !== null) {
        let tepTinId = editingItem?.tepTinId ?? null;
        if (selectedImageFile) {
          const uploadedImage = await uploadVatPhamImage({
            imageFile: selectedImageFile,
            tenVatPham,
          });
          tepTinId = uploadedImage.id;
        }

        const updated = await updateVatPham(editingItemId, {
          tenVatPham,
          soLuong,
          donViId: Number(selectedDonViId),
          nhomVatPhamId: Number(selectedNhomVatPhamId),
          tepTinId,
          trangThai: editingItem?.trangThai ?? true,
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
        showActionToast(
          "success",
          "Cập nhật thành công",
          `Đã cập nhật vật phẩm "${updatedItem.tenVatPham}".`
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
      setCurrentPage(1);
      showActionToast(
        "success",
        "Thêm thành công",
        `Đã thêm vật phẩm "${createdItem.tenVatPham}".`
      );
      resetForm({ revokeImage: true });
    } catch (error) {
      showActionToast("error", "Thao tác thất bại", getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEditItem = (item: ThemVatPhamItem) => {
    if (isSubmitting) {
      return;
    }
    if (item.donViId === null || item.nhomVatPhamId === null) {
      showActionToast(
        "error",
        "Không thể chỉnh sửa",
        "Vật phẩm này đang thiếu đơn vị hoặc nhóm vật phẩm."
      );
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
  };

  const handleDeleteItem = async (item: ThemVatPhamItem) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);

    try {
      await deleteVatPham(item.id);
      setItems((prev) => prev.filter((currentItem) => currentItem.id !== item.id));
      if (item.imageUrl) {
        revokeObjectUrl(item.imageUrl);
      }
      if (editingItemId === item.id) {
        resetForm({ revokeImage: true });
      }
      showActionToast(
        "success",
        "Xóa thành công",
        `Đã xóa vật phẩm "${item.tenVatPham}".`
      );
    } catch (error) {
      showActionToast("error", "Không thể xóa", getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleToggleVisibilityItem = async (item: ThemVatPhamItem) => {
    if (isSubmitting) {
      return;
    }
    if (item.donViId === null || item.nhomVatPhamId === null) {
      showActionToast(
        "error",
        "Không thể cập nhật trạng thái",
        "Vật phẩm này đang thiếu đơn vị hoặc nhóm vật phẩm."
      );
      return;
    }

    setIsSubmitting(true);

    try {
      const updated = await updateVatPham(item.id, {
        tenVatPham: item.tenVatPham,
        soLuong: item.soLuong,
        donViId: item.donViId,
        nhomVatPhamId: item.nhomVatPhamId,
        tepTinId: item.tepTinId ?? null,
        trangThai: !item.trangThai,
      });
      const updatedItem = mapVatPhamDtoToItem(updated);
      setItems((prev) =>
        prev.map((currentItem) =>
          currentItem.id === item.id
            ? {
                ...currentItem,
                ...updatedItem,
              }
            : currentItem
        )
      );
      showActionToast(
        "success",
        updatedItem.trangThai ? "Đã hiện vật phẩm" : "Đã ẩn vật phẩm",
        updatedItem.trangThai
          ? `Vật phẩm "${updatedItem.tenVatPham}" đã được hiện.`
          : `Vật phẩm "${updatedItem.tenVatPham}" đã được ẩn.`
      );
      if (editingItemId === item.id) {
        resetForm({ revokeImage: true });
      }
    } catch (error) {
      showActionToast(
        "error",
        "Không thể cập nhật trạng thái",
        getApiErrorMessage(error)
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <PageMeta
        title="Thêm vật phẩm"
        description="Trang thêm vật phẩm với lựa chọn đơn vị và nhóm vật phẩm."
      />
      <PageBreadcrumb pageTitle="Thêm vật phẩm" />

      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Không thể tải dữ liệu vật phẩm: {loadError}
          </div>
        )}

        <ComponentCard
          title={editingItemId !== null ? "Chỉnh sửa vật phẩm" : "Nhập vật phẩm"}
          desc={
            editingItemId !== null
              ? "Cập nhật thông tin vật phẩm và có thể thay ảnh vật phẩm."
              : "Nhập thông tin vật phẩm"
          }
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Đang tải danh sách vật phẩm, đơn vị và nhóm vật phẩm...
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-vat-pham">Tên vật phẩm</Label>
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
                <Label htmlFor="so-luong">Số lượng</Label>
                <Input
                  id="so-luong"
                  type="number"
                  min="1"
                  max={String(MAX_SHORT)}
                  step={1}
                  placeholder="Ví dụ: 100"
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
                      {isDragActive
                        ? "Thả file ảnh vào đây"
                        : editingItemId !== null
                          ? "Kéo thả ảnh mới vào đây để thay thế"
                          : "Kéo thả ảnh vào đây"}
                    </p>
                    <p className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                      Hoặc bấm để chọn file PNG, JPG, WebP, SVG, GIF
                    </p>
                  </div>
                </div>

                {selectedImagePreviewUrl ? (
                  <div className="mt-3 flex items-start gap-3 rounded-xl border border-gray-200 bg-gray-50 p-3 dark:border-white/[0.08] dark:bg-white/[0.03]">
                    <img
                      src={selectedImagePreviewUrl}
                      alt="Xem trước ảnh mới"
                      className="h-16 w-16 rounded-lg object-cover"
                    />
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium text-gray-800 text-theme-sm dark:text-white/90">
                        {selectedImageFile?.name}
                      </p>
                      <p className="mt-1 break-all text-theme-xs text-gray-500 dark:text-gray-400">
                        Tệp sẽ được cập nhật: {selectedImageStoragePath}
                      </p>
                      <button
                        type="button"
                        onClick={() => clearSelectedImage(true)}
                        className="mt-2 inline-flex items-center rounded-lg border border-gray-300 px-3 py-1.5 text-theme-xs font-medium text-gray-700 hover:bg-gray-100 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                      >
                        Bỏ ảnh mới
                      </button>
                    </div>
                  </div>
                ) : (
                  editingItem?.imageUrl && (
                    <div className="mt-3 flex items-start gap-3 rounded-xl border border-gray-200 bg-gray-50 p-3 dark:border-white/[0.08] dark:bg-white/[0.03]">
                      <img
                        src={editingItem.imageUrl}
                        alt={`Ảnh hiện tại của ${editingItem.tenVatPham}`}
                        className="h-16 w-16 rounded-lg object-cover"
                      />
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-medium text-gray-800 text-theme-sm dark:text-white/90">
                          Ảnh hiện tại
                        </p>
                        <p className="mt-1 break-all text-theme-xs text-gray-500 dark:text-gray-400">
                          {editingItem.imagePath || editingItem.imageUrl}
                        </p>
                      </div>
                    </div>
                  )
                )}

                {editingItemId !== null && (
                  <p className="mt-2 text-theme-xs text-gray-500 dark:text-gray-400">
                    Nếu không chọn ảnh mới, hệ thống sẽ giữ nguyên ảnh hiện tại.
                  </p>
                )}

                {errors.imageFile && (
                  <p className="mt-1.5 text-xs text-error-500">{errors.imageFile}</p>
                )}
              </div>

              <div>
                <Label htmlFor="don-vi-combobox">Chọn đơn vị</Label>
                <Combobox
                  id="don-vi-combobox"
                  value={selectedDonViId}
                  options={donViComboboxOptions}
                  onChange={handleSelectDonVi}
                  placeholder={
                    isLoading ? "Dang tai danh sach don vi..." : "Chon don vi"
                  }
                  disabled={isLoading || isSubmitting}
                  error={Boolean(errors.donViId)}
                  hint={errors.donViId}
                  emptyMessage="Khong tim thay don vi phu hop."
                />
              </div>

              <div>
                <Label htmlFor="nhom-vat-pham-combobox">Thêm vào nhóm vật phẩm</Label>
                <Combobox
                  id="nhom-vat-pham-combobox"
                  value={selectedNhomVatPhamId}
                  options={nhomVatPhamComboboxOptions}
                  onChange={handleSelectNhomVatPham}
                  placeholder={
                    isLoading
                      ? "Dang tai danh sach nhom vat pham..."
                      : "Chon nhom vat pham"
                  }
                  disabled={isLoading || isSubmitting}
                  error={Boolean(errors.nhomVatPhamId)}
                  hint={errors.nhomVatPhamId}
                  emptyMessage="Khong tim thay nhom vat pham phu hop."
                />
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
              Bản ghi tệp tin hiện tại: {tepTinCount}
            </p>

            <div className="flex flex-wrap items-center gap-3">
              <button
                type="submit"
                disabled={isSubmitting}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isSubmitting
                  ? "Đang xử lý..."
                  : editingItemId !== null
                    ? "Cập nhật vật phẩm"
                    : "Thêm vật phẩm"}
              </button>

              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => resetForm({ revokeImage: true })}
                className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
              >
                Đặt lại
              </button>

              {editingItemId !== null && (
                <button
                  type="button"
                  disabled={isSubmitting}
                  onClick={() => resetForm({ revokeImage: true })}
                  className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                >
                  Hủy chỉnh sửa
                </button>
              )}
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sách vật phẩm đã thêm">
          <div className="mb-4 flex flex-col gap-3 border-b border-gray-100 pb-4 dark:border-white/[0.08] sm:flex-row sm:items-end sm:justify-between">
            <p className="text-theme-xs text-gray-500 dark:text-gray-400">
              Hien thi {paginatedItems.length}/{items.length} vat pham
            </p>

            <div className="w-full sm:w-[180px]">
              <Label htmlFor="them-vat-pham-rows-per-page">So dong/trang</Label>
              <select
                id="them-vat-pham-rows-per-page"
                value={rowsPerPage}
                onChange={(event) => setRowsPerPage(Number(event.target.value))}
                className="h-11 w-full appearance-none rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 pr-11 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
              >
                {ROWS_PER_PAGE_OPTIONS.map((option) => (
                  <option
                    key={option}
                    value={option}
                    className="text-gray-700 dark:bg-gray-900 dark:text-gray-400"
                  >
                    {option}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <ThemVatPhamTable
            items={paginatedItems}
            onEditItem={handleEditItem}
            onToggleVisibilityItem={(item) => {
              void handleToggleVisibilityItem(item);
            }}
            onDeleteItem={(item) => {
              void handleDeleteItem(item);
            }}
          />
          <Pagination
            currentPage={currentPage}
            totalItems={items.length}
            itemsPerPage={rowsPerPage}
            onPageChange={setCurrentPage}
          />
        </ComponentCard>
      </div>

      <ActionToast toast={actionToast} onClose={() => setActionToast(null)} />
    </>
  );
}
