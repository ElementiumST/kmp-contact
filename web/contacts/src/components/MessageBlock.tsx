import "./MessageBlock.css";

interface MessageBlockProps {
  text: string;
  tone?: "default" | "warning" | "error";
  buttonLabel?: string;
  onButtonClick?: () => void;
}

export function MessageBlock({
  text,
  tone = "default",
  buttonLabel,
  onButtonClick,
}: MessageBlockProps) {
  return (
    <section className={`section message-block message-block--${tone}`}>
      <p className="message-block__text">{text}</p>

      {buttonLabel && onButtonClick ? (
        <button className="primary-button" type="button" onClick={onButtonClick}>
          {buttonLabel}
        </button>
      ) : null}
    </section>
  );
}
