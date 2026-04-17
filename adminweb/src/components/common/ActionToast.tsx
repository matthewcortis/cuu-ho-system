import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { CheckCircleIcon, CloseLineIcon, ErrorIcon } from "@/icons";

export type ActionToastType = "success" | "error";

export interface ActionToastData {
  id: number;
  type: ActionToastType;
  title: string;
  message: string;
}

interface ActionToastProps {
  toast: ActionToastData | null;
  onClose: () => void;
  autoHideMs?: number;
}

export default function ActionToast({
  toast,
  onClose,
  autoHideMs = 3500,
}: ActionToastProps) {
  const [isVisible, setIsVisible] = useState(false);
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    if (!toast) {
      setIsVisible(false);
      return;
    }

    setIsVisible(true);
    const hideTimer = window.setTimeout(() => {
      setIsVisible(false);
    }, autoHideMs);

    return () => {
      window.clearTimeout(hideTimer);
    };
  }, [toast?.id, autoHideMs]);

  useEffect(() => {
    if (!toast || isVisible) {
      return;
    }

    const closeTimer = window.setTimeout(() => {
      onClose();
    }, 200);

    return () => {
      window.clearTimeout(closeTimer);
    };
  }, [toast, isVisible, onClose]);

  if (!toast || !isMounted) {
    return null;
  }

  const isSuccess = toast.type === "success";
  const panelToneClass = isSuccess
    ? "border-success-200 dark:border-success-500/30"
    : "border-error-200 dark:border-error-500/30";
  const iconToneClass = isSuccess
    ? "bg-success-50 text-success-600 dark:bg-success-500/10 dark:text-success-400"
    : "bg-error-50 text-error-600 dark:bg-error-500/10 dark:text-error-400";

  return createPortal(
    <div
      aria-live="assertive"
      className="pointer-events-none fixed inset-0 z-[2147483647] flex items-end px-4 py-6 sm:items-start sm:p-6"
    >
      <div className="flex w-full flex-col items-center space-y-4 sm:items-end">
        <div
          className={`pointer-events-auto w-full max-w-sm rounded-lg border bg-white shadow-lg transition-all duration-300 ease-out dark:bg-gray-800 ${panelToneClass} ${
            isVisible
              ? "translate-y-0 opacity-100 sm:translate-x-0"
              : "translate-y-2 opacity-0 sm:translate-x-2 sm:translate-y-0"
          }`}
        >
          <div className="p-4">
            <div className="flex items-start">
              <div
                className={`shrink-0 inline-flex h-8 w-8 items-center justify-center rounded-full ${iconToneClass}`}
              >
                {isSuccess ? (
                  <CheckCircleIcon aria-hidden="true" className="size-5" />
                ) : (
                  <ErrorIcon aria-hidden="true" className="size-5" />
                )}
              </div>
              <div className="ml-3 w-0 flex-1 pt-0.5">
                <p className="text-sm font-medium text-gray-800 dark:text-white/90">
                  {toast.title}
                </p>
                <p className="mt-1 text-sm text-gray-600 dark:text-gray-300">
                  {toast.message}
                </p>
              </div>
              <div className="ml-4 flex shrink-0">
                <button
                  type="button"
                  onClick={() => setIsVisible(false)}
                  className="inline-flex rounded-md text-gray-400 hover:text-gray-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-500 dark:text-gray-500 dark:hover:text-white"
                >
                  <span className="sr-only">Close</span>
                  <CloseLineIcon aria-hidden="true" className="size-5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
}
