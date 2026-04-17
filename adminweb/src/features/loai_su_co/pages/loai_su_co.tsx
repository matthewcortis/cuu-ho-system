import { ChangeEvent, FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import Cropper, { type Area } from "react-easy-crop";
import "react-easy-crop/react-easy-crop.css";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import ActionToast, {
  type ActionToastData,
} from "@/components/common/ActionToast";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import LoaiSuCoTable, {
  type LoaiSuCoItem,
} from "@/features/loai_su_co/components/loai_su_co";
import {
  createLoaiSuCo,
  deleteLoaiSuCo,
  fetchLoaiSuCoList,
  LoaiSuCoApiError,
  type LoaiSuCoDto,
  uploadLoaiSuCoIcon,
  updateLoaiSuCo,
} from "@/features/loai_su_co/api/loaiSuCoApi";

interface LoaiSuCoFormValues {
  ten: string;
  iconUrl: string;
}

interface LoaiSuCoFormErrors {
  ten: string;
  iconUrl: string;
}

const defaultFormValues: LoaiSuCoFormValues = {
  ten: "",
  iconUrl: "",
};

const defaultFormErrors: LoaiSuCoFormErrors = {
  ten: "",
  iconUrl: "",
};

function mapLoaiSuCoDtoToItem(loaiSuCo: LoaiSuCoDto): LoaiSuCoItem {
  return {
    id: loaiSuCo.id,
    ten: loaiSuCo.ten.trim() || "Chua dat ten",
    iconUrl: loaiSuCo.iconUrl.trim(),
    createdAt: loaiSuCo.createdAt,
  };
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof LoaiSuCoApiError) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return "Khong the xu ly yeu cau loai su co. Vui long thu lai.";
}

function isValidUrl(url: string): boolean {
  if (!url.trim()) {
    return true;
  }
  try {
    const value = new URL(url);
    return value.protocol === "http:" || value.protocol === "https:";
  } catch {
    return false;
  }
}

function toPngFileName(fileName: string): string {
  const trimmedName = fileName.trim();
  if (!trimmedName) {
    return "icon-loai-su-co.png";
  }

  const baseName = trimmedName.replace(/\.[^/.]+$/, "");
  return `${baseName || "icon-loai-su-co"}.png`;
}

function createImageElement(imageSource: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.addEventListener("load", () => resolve(image));
    image.addEventListener("error", () => reject(new Error("Khong the doc anh de cat icon.")));
    image.src = imageSource;
  });
}

async function cropImageToSquare(imageSource: string, cropAreaPixels: Area): Promise<Blob> {
  const image = await createImageElement(imageSource);
  const width = Math.max(1, Math.round(cropAreaPixels.width));
  const height = Math.max(1, Math.round(cropAreaPixels.height));

  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;

  const context = canvas.getContext("2d");
  if (!context) {
    throw new Error("Trinh duyet khong ho tro cat anh.");
  }

  context.drawImage(
    image,
    Math.round(cropAreaPixels.x),
    Math.round(cropAreaPixels.y),
    width,
    height,
    0,
    0,
    width,
    height
  );

  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error("Khong the tao anh da cat."));
        return;
      }
      resolve(blob);
    }, "image/png");
  });
}

export default function LoaiSuCoPage() {
  const [items, setItems] = useState<LoaiSuCoItem[]>([]);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [formValues, setFormValues] = useState<LoaiSuCoFormValues>(defaultFormValues);
  const [formErrors, setFormErrors] = useState<LoaiSuCoFormErrors>(defaultFormErrors);
  const [isLoading, setIsLoading] = useState(false);
  const [isMutating, setIsMutating] = useState(false);
  const [isUploadingIcon, setIsUploadingIcon] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [actionToast, setActionToast] = useState<ActionToastData | null>(null);
  const [cropImageSource, setCropImageSource] = useState<string | null>(null);
  const [cropImageName, setCropImageName] = useState("icon-loai-su-co.png");
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null);

  useEffect(() => {
    return () => {
      if (cropImageSource) {
        URL.revokeObjectURL(cropImageSource);
      }
    };
  }, [cropImageSource]);

  useEffect(() => {
    let isMounted = true;

    const loadLoaiSuCoList = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const remoteItems = await fetchLoaiSuCoList();
        if (!isMounted) {
          return;
        }
        setItems(remoteItems.map(mapLoaiSuCoDtoToItem));
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

    void loadLoaiSuCoList();

    return () => {
      isMounted = false;
    };
  }, []);

  const filteredItems = useMemo(() => {
    const normalizedKeyword = searchKeyword.trim().toLowerCase();
    if (!normalizedKeyword) {
      return items;
    }

    return items.filter((item) => item.ten.toLowerCase().includes(normalizedKeyword));
  }, [items, searchKeyword]);

  const handleChangeFormValue = (field: keyof LoaiSuCoFormValues, value: string) => {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));

    if (value.trim()) {
      setFormErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
    }
  };

  const showToast = (type: "success" | "error", title: string, message: string) => {
    setActionToast({
      id: Date.now(),
      type,
      title,
      message,
    });
  };

  const closeCropDialog = useCallback(() => {
    setCropImageSource(null);
    setCropImageName("icon-loai-su-co.png");
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
  }, []);

  const handleCropComplete = useCallback((_area: Area, areaPixels: Area) => {
    setCroppedAreaPixels(areaPixels);
  }, []);

  const handleSelectIconFile = (event: ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];
    event.target.value = "";

    if (!selectedFile) {
      return;
    }
    if (!selectedFile.type.startsWith("image/")) {
      showToast("error", "File khong hop le", "Vui long chon file anh de tai len.");
      return;
    }

    const source = URL.createObjectURL(selectedFile);
    setCropImageSource(source);
    setCropImageName(selectedFile.name || "icon-loai-su-co.png");
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
  };

  const handleUploadCroppedIcon = async () => {
    if (isUploadingIcon || !cropImageSource || !croppedAreaPixels) {
      return;
    }

    setIsUploadingIcon(true);
    try {
      const croppedBlob = await cropImageToSquare(cropImageSource, croppedAreaPixels);
      const croppedFile = new File([croppedBlob], toPngFileName(cropImageName), {
        type: "image/png",
      });
      const uploadedIcon = await uploadLoaiSuCoIcon({
        iconFile: croppedFile,
        tenLoaiSuCo: formValues.ten,
      });

      handleChangeFormValue("iconUrl", uploadedIcon.duongDan);
      closeCropDialog();
      showToast("success", "Tai icon thanh cong", "Da cat anh vuong va tai icon len Cloudinary.");
    } catch (error) {
      showToast("error", "Khong the tai icon", getApiErrorMessage(error));
    } finally {
      setIsUploadingIcon(false);
    }
  };

  const resetForm = () => {
    setFormValues(defaultFormValues);
    setFormErrors(defaultFormErrors);
    setEditingId(null);
    closeCropDialog();
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isMutating || isUploadingIcon) {
      return;
    }

    const ten = formValues.ten.trim();
    const iconUrl = formValues.iconUrl.trim();
    const nextErrors: LoaiSuCoFormErrors = {
      ten: "",
      iconUrl: "",
    };

    if (!ten) {
      nextErrors.ten = "Vui long nhap ten loai su co.";
    }
    if (iconUrl && !isValidUrl(iconUrl)) {
      nextErrors.iconUrl = "Icon URL phai la duong dan http/https hop le.";
    }

    setFormErrors(nextErrors);
    if (nextErrors.ten || nextErrors.iconUrl) {
      return;
    }

    setIsMutating(true);

    try {
      if (editingId !== null) {
        const updated = await updateLoaiSuCo(editingId, {
          ten,
          iconUrl: iconUrl || null,
        });
        const updatedItem = mapLoaiSuCoDtoToItem(updated);
        setItems((prev) =>
          prev.map((item) =>
            item.id === editingId
              ? {
                ...item,
                ...updatedItem,
              }
              : item
          )
        );
        resetForm();
        showToast("success", "Cap nhat thanh cong", "Da cap nhat loai su co.");
        return;
      }

      const created = await createLoaiSuCo({
        ten,
        iconUrl: iconUrl || null,
      });
      const createdItem = mapLoaiSuCoDtoToItem(created);
      setItems((prev) => [createdItem, ...prev.filter((item) => item.id !== createdItem.id)]);
      resetForm();
      showToast("success", "Them thanh cong", "Da tao loai su co moi.");
    } catch (error) {
      showToast("error", "Thao tac that bai", getApiErrorMessage(error));
    } finally {
      setIsMutating(false);
    }
  };

  const handleEditItem = (item: LoaiSuCoItem) => {
    if (isMutating || isUploadingIcon) {
      return;
    }

    closeCropDialog();
    setEditingId(item.id);
    setFormValues({
      ten: item.ten,
      iconUrl: item.iconUrl,
    });
    setFormErrors(defaultFormErrors);
  };

  const handleDeleteItem = async (item: LoaiSuCoItem) => {
    if (isMutating || isUploadingIcon) {
      return;
    }

    setIsMutating(true);

    try {
      await deleteLoaiSuCo(item.id);
      setItems((prev) => prev.filter((currentItem) => currentItem.id !== item.id));
      if (editingId === item.id) {
        resetForm();
      }
      showToast("success", "Xoa thanh cong", `Da xoa loai su co "${item.ten}".`);
    } catch (error) {
      showToast("error", "Khong the xoa", getApiErrorMessage(error));
    } finally {
      setIsMutating(false);
    }
  };

  return (
    <>
      <PageMeta title="Loại sự cố" description="Trang quản lý loại sự cố." />
      <PageBreadcrumb pageTitle="Loại sự cố" />

      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Không thể lấy danh sách loại sự cố từ backend: {loadError}
          </div>
        )}

        <ComponentCard
          title={editingId !== null ? "Cập nhật loại sự cố" : "Tạo loại sự cố mới"}
          desc="Quản lý danh sách loại sự cố."
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Đang tải danh sách loại sự cố...
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-loai-su-co">Tên loại sự cố</Label>
                <Input
                  id="ten-loai-su-co"
                  type="text"
                  placeholder="Ví dụ: Lũ lụt"
                  value={formValues.ten}
                  onChange={(event) => handleChangeFormValue("ten", event.target.value)}
                  error={Boolean(formErrors.ten)}
                  hint={formErrors.ten}
                />
              </div>

              <div>
                <Label htmlFor="icon-url-loai-su-co">URL icon</Label>
                <Input
                  id="icon-url-loai-su-co"
                  type="text"
                  placeholder="Ví dụ: https://example.com/icon.png"
                  value={formValues.iconUrl}
                  onChange={(event) => handleChangeFormValue("iconUrl", event.target.value)}
                  error={Boolean(formErrors.iconUrl)}
                  hint={formErrors.iconUrl}
                />

                <div className="mt-3 space-y-3">
                  <input
                    id="icon-file-loai-su-co"
                    type="file"
                    accept="image/*"
                    onChange={handleSelectIconFile}
                    disabled={isMutating || isUploadingIcon}
                    className="block w-full rounded-lg border border-gray-300 bg-transparent px-3 py-2 text-sm text-gray-700 file:mr-3 file:rounded-md file:border-0 file:bg-brand-500 file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-white hover:file:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60 dark:border-gray-700 dark:text-gray-300"
                  />

                  <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                    Tải icon từ máy tính. Ảnh sẽ được cắt vuông (tỷ lệ 1:1) trước khi upload lên
                    Cloudinary vào thư mục iconloaisuco.
                  </p>

                  {formValues.iconUrl && (
                    <div className="inline-flex items-center gap-3 rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 dark:border-gray-800 dark:bg-gray-900/40">
                      <img
                        src={formValues.iconUrl}
                        alt="Icon loại sự cố"
                        className="h-12 w-12 rounded-md border border-gray-200 object-cover dark:border-gray-700"
                      />
                      <span className="text-theme-xs text-gray-600 dark:text-gray-300">
                        Đã cập nhật icon
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <button
                type="submit"
                disabled={isMutating || isUploadingIcon}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isUploadingIcon
                  ? "Đang tải icon..."
                  : editingId !== null
                    ? isMutating
                      ? "Đang cập nhật..."
                      : "Cập nhật loại sự cố"
                    : isMutating
                      ? "Đang thêm..."
                      : "Thêm loại sự cố"}
              </button>

              {editingId !== null && (
                <button
                  type="button"
                  onClick={resetForm}
                  disabled={isMutating || isUploadingIcon}
                  className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                >
                  Hủy chỉnh sửa
                </button>
              )}
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sách loại sự cố">
          <div className="mb-4">
            <Label htmlFor="tim-kiem-loai-su-co">Tìm kiếm theo tên loại sự cố</Label>
            <Input
              id="tim-kiem-loai-su-co"
              type="text"
              placeholder="Nhập tên loại sự cố cần tìm"
              value={searchKeyword}
              onChange={(event) => setSearchKeyword(event.target.value)}
            />
          </div>

          <p className="mb-4 text-theme-xs text-gray-500 dark:text-gray-400">
            Hiển thị {filteredItems.length}/{items.length} loại sự cố
          </p>

          <LoaiSuCoTable
            items={filteredItems}
            onEditItem={handleEditItem}
            onDeleteItem={(item) => {
              void handleDeleteItem(item);
            }}
          />
        </ComponentCard>
      </div>

      {cropImageSource && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-950/70 p-4">
          <div
            role="dialog"
            aria-modal="true"
            className="w-full max-w-2xl rounded-2xl bg-white p-5 shadow-lg dark:bg-gray-900"
          >
            <h3 className="text-base font-semibold text-gray-900 dark:text-gray-100">
              Cắt icon hình vuông
            </h3>
            <p className="mt-1 text-theme-sm text-gray-500 dark:text-gray-400">
              Căn chỉnh khung cắt theo icon mong muốn. Ảnh sau khi lưu sẽ là hình vuông.
            </p>

            <div className="relative mt-4 h-72 w-full overflow-hidden rounded-lg bg-gray-100 dark:bg-gray-800">
              <Cropper
                image={cropImageSource}
                crop={crop}
                zoom={zoom}
                aspect={1}
                onCropChange={setCrop}
                onZoomChange={setZoom}
                onCropComplete={handleCropComplete}
                showGrid={false}
              />
            </div>

            <div className="mt-4">
              <Label htmlFor="zoom-crop-icon-loai-su-co">Độ phóng</Label>
              <input
                id="zoom-crop-icon-loai-su-co"
                type="range"
                min={1}
                max={3}
                step={0.1}
                value={zoom}
                onChange={(event) => setZoom(Number(event.target.value))}
                className="h-2 w-full cursor-pointer appearance-none rounded-lg bg-gray-200 dark:bg-gray-700"
              />
            </div>

            <div className="mt-5 flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={closeCropDialog}
                disabled={isUploadingIcon}
                className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={() => {
                  void handleUploadCroppedIcon();
                }}
                disabled={isUploadingIcon || !croppedAreaPixels}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isUploadingIcon ? "Đang tải lên..." : "Cắt và tải lên"}
              </button>
            </div>
          </div>
        </div>
      )}

      <ActionToast toast={actionToast} onClose={() => setActionToast(null)} />
    </>
  );
}
