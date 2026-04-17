import { AngleLeftIcon, AngleRightIcon } from "@/icons";

type PaginationItem = number | "ellipsis-left" | "ellipsis-right";

interface PaginationProps {
  currentPage: number;
  totalItems: number;
  itemsPerPage: number;
  onPageChange: (page: number) => void;
  totalItemsOverall?: number;
  className?: string;
}

function buildPaginationItems(
  currentPage: number,
  totalPages: number
): PaginationItem[] {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1);
  }

  const items: PaginationItem[] = [1];
  let start = Math.max(2, currentPage - 1);
  let end = Math.min(totalPages - 1, currentPage + 1);

  if (currentPage <= 3) {
    start = 2;
    end = 4;
  } else if (currentPage >= totalPages - 2) {
    start = totalPages - 3;
    end = totalPages - 1;
  }

  if (start > 2) {
    items.push("ellipsis-left");
  }

  for (let page = start; page <= end; page += 1) {
    items.push(page);
  }

  if (end < totalPages - 1) {
    items.push("ellipsis-right");
  }

  items.push(totalPages);
  return items;
}

export default function Pagination({
  currentPage,
  totalItems,
  itemsPerPage,
  onPageChange,
  totalItemsOverall,
  className = "",
}: PaginationProps) {
  const totalPages = Math.max(1, Math.ceil(totalItems / itemsPerPage));
  const safeCurrentPage = Math.min(Math.max(currentPage, 1), totalPages);
  const showingFrom = totalItems === 0 ? 0 : (safeCurrentPage - 1) * itemsPerPage + 1;
  const showingTo =
    totalItems === 0 ? 0 : Math.min(safeCurrentPage * itemsPerPage, totalItems);
  const canGoPrevious = totalItems > 0 && safeCurrentPage > 1;
  const canGoNext = totalItems > 0 && safeCurrentPage < totalPages;
  const pageItems = buildPaginationItems(safeCurrentPage, totalPages);

  const handleGoToPage = (page: number) => {
    if (page < 1 || page > totalPages || page === safeCurrentPage) {
      return;
    }

    onPageChange(page);
  };

  return (
    <div
      className={`mt-4 flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6 dark:border-white/10 dark:bg-transparent ${className}`}
    >
      <div className="flex flex-1 justify-between sm:hidden">
        <button
          type="button"
          onClick={() => handleGoToPage(safeCurrentPage - 1)}
          disabled={!canGoPrevious}
          className="relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-white/10 dark:bg-white/5 dark:text-gray-200 dark:hover:bg-white/10"
        >
          Trước
        </button>
        <button
          type="button"
          onClick={() => handleGoToPage(safeCurrentPage + 1)}
          disabled={!canGoNext}
          className="relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-white/10 dark:bg-white/5 dark:text-gray-200 dark:hover:bg-white/10"
        >
          Sau
        </button>
      </div>

      <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-gray-700 dark:text-gray-300">
            Hiển thị <span className="font-medium">{showingFrom}</span> đến{" "}
            <span className="font-medium">{showingTo}</span> trong{" "}
            <span className="font-medium">{totalItems}</span> kết quả
            {typeof totalItemsOverall === "number" && (
              <>
                {" "}
                (tổng: <span className="font-medium">{totalItemsOverall}</span>)
              </>
            )}
          </p>
        </div>

        <div>
          <nav
            aria-label="Pagination"
            className="isolate inline-flex -space-x-px rounded-md shadow-xs dark:shadow-none"
          >
            <button
              type="button"
              onClick={() => handleGoToPage(safeCurrentPage - 1)}
              disabled={!canGoPrevious}
              className="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 inset-ring inset-ring-gray-300 transition hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:cursor-not-allowed disabled:opacity-50 dark:inset-ring-gray-700 dark:hover:bg-white/5"
            >
              <span className="sr-only">Trang trước</span>
              <AngleLeftIcon aria-hidden="true" className="size-5 fill-current" />
            </button>

            {pageItems.map((item) =>
              typeof item === "number" ? (
                <button
                  key={item}
                  type="button"
                  onClick={() => handleGoToPage(item)}
                  aria-current={item === safeCurrentPage ? "page" : undefined}
                  className={`relative inline-flex items-center px-4 py-2 text-sm font-semibold transition focus:z-20 focus:outline-offset-0 ${
                    item === safeCurrentPage
                      ? "z-10 bg-brand-600 text-white dark:bg-brand-500"
                      : "text-gray-900 inset-ring inset-ring-gray-300 hover:bg-gray-50 dark:text-gray-200 dark:inset-ring-gray-700 dark:hover:bg-white/5"
                  }`}
                >
                  {item}
                </button>
              ) : (
                <span
                  key={item}
                  className="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700 inset-ring inset-ring-gray-300 dark:text-gray-400 dark:inset-ring-gray-700"
                >
                  ...
                </span>
              )
            )}

            <button
              type="button"
              onClick={() => handleGoToPage(safeCurrentPage + 1)}
              disabled={!canGoNext}
              className="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 inset-ring inset-ring-gray-300 transition hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:cursor-not-allowed disabled:opacity-50 dark:inset-ring-gray-700 dark:hover:bg-white/5"
            >
              <span className="sr-only">Trang sau</span>
              <AngleRightIcon aria-hidden="true" className="size-5 fill-current" />
            </button>
          </nav>
        </div>
      </div>
    </div>
  );
}
