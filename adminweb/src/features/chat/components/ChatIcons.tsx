export function SearchIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="1.8" />
      <path
        d="M20 20L17 17"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  );
}

export function PhoneIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <path
        d="M6.6 3H4.9C4.4 3 3.9 3.2 3.6 3.6C3.2 4 3 4.5 3 5C3 9.8 5 14.4 8.5 17.9C12 21.4 16.6 23.4 21.4 23.4C21.9 23.4 22.4 23.2 22.8 22.8C23.2 22.5 23.4 22 23.4 21.5V19.8C23.4 19.1 22.9 18.5 22.2 18.3L18.4 17.3C17.8 17.1 17.2 17.3 16.8 17.8L15.9 18.9C13.3 17.7 10.9 15.3 9.7 12.7L10.8 11.8C11.3 11.4 11.5 10.8 11.3 10.2L10.3 6.4C10.1 5.7 9.5 5.2 8.8 5.2H7.1"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function EmojiIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.8" />
      <path
        d="M8.5 14.5C9.2 15.4 10.5 16 12 16C13.5 16 14.8 15.4 15.5 14.5"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
      <circle cx="9" cy="10" r="1" fill="currentColor" />
      <circle cx="15" cy="10" r="1" fill="currentColor" />
    </svg>
  );
}

export function AttachmentIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <path
        d="M8 12.5L13.5 7C14.9 5.6 17.1 5.6 18.5 7C19.9 8.4 19.9 10.6 18.5 12L11 19.5C9.1 21.4 6.1 21.4 4.2 19.5C2.3 17.6 2.3 14.6 4.2 12.7L11.2 5.7"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function MicIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <rect
        x="9"
        y="3.5"
        width="6"
        height="11"
        rx="3"
        stroke="currentColor"
        strokeWidth="1.8"
      />
      <path
        d="M6 11.5C6 14.8 8.7 17.5 12 17.5C15.3 17.5 18 14.8 18 11.5"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
      <path d="M12 17.5V21" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      <path
        d="M9.5 21H14.5"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  );
}
