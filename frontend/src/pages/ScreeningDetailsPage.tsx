import { isAxiosError } from "axios";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { createBooking } from "../api/bookingApi";
import { getScreeningSeats } from "../api/screeningApi";
import type { ScreeningSeat } from "../types/screeningSeat";

function ScreeningDetailsPage() {
  const { screeningId } = useParams<{
    screeningId: string;
  }>();

  const navigate = useNavigate();

  const [seats, setSeats] = useState<ScreeningSeat[]>([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [bookingError, setBookingError] = useState<string | null>(null);

  useEffect(() => {
    async function loadSeats() {
      const id = Number(screeningId);

      if (!Number.isInteger(id) || id <= 0) {
        setError("Invalid screening ID.");
        setIsLoading(false);
        return;
      }

      try {
        const screeningSeats = await getScreeningSeats(id);
        setSeats(screeningSeats);
      } catch {
        setError("Unable to load seats for this screening.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadSeats();
  }, [screeningId]);

  const selectedSeats = seats.filter((seat) =>
    selectedSeatIds.includes(seat.screeningSeatId),
  );

  const totalPrice = selectedSeats.reduce(
    (total, seat) => total + seat.price,
    0,
  );

  function toggleSeat(seatId: number) {
    setSelectedSeatIds((currentIds) =>
      currentIds.includes(seatId)
        ? currentIds.filter((id) => id !== seatId)
        : [...currentIds, seatId],
    );

    setBookingError(null);
  }

  async function handleCreateBooking() {
    const id = Number(screeningId);

    if (!Number.isInteger(id) || id <= 0 || selectedSeatIds.length === 0) {
      return;
    }

    setIsSubmitting(true);
    setBookingError(null);

    try {
      const booking = await createBooking({
        screeningId: id,
        screeningSeatIds: selectedSeatIds,
      });

      navigate(`/bookings/${booking.id}/payment`);
    } catch (error) {
      if (isAxiosError<{ message?: string }>(error)) {
        setBookingError(
          error.response?.data.message ?? "Unable to create booking.",
        );
      } else {
        setBookingError("Unable to create booking.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section>
      <h1 className="text-3xl font-bold">Select your seats</h1>

      <div className="mx-auto mt-10 max-w-2xl">
        <div className="rounded-t-full bg-slate-300 py-2 text-center text-sm font-semibold text-slate-900">
          SCREEN
        </div>

        {isLoading && (
          <p className="mt-10 text-center text-slate-300">Loading seats...</p>
        )}

        {error && (
          <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
            {error}
          </div>
        )}

        {!isLoading && !error && (
          <div className="mt-12 grid grid-cols-5 gap-3 sm:grid-cols-10">
            {seats.map((seat) => {
              const isAvailable = seat.status === "AVAILABLE";

              const isSelected = selectedSeatIds.includes(seat.screeningSeatId);

              return (
                <button
                  key={seat.screeningSeatId}
                  type="button"
                  disabled={!isAvailable || isSubmitting}
                  onClick={() => toggleSeat(seat.screeningSeatId)}
                  title={`${seat.seatType} — ${seat.price.toLocaleString()} RSD`}
                  className={`aspect-square rounded-md text-sm font-semibold transition ${
                    isSelected
                      ? "bg-amber-500 text-slate-950"
                      : isAvailable
                        ? "bg-emerald-600 text-white hover:bg-emerald-500"
                        : "cursor-not-allowed bg-slate-700 text-slate-500"
                  }`}
                >
                  {seat.rowLabel}
                  {seat.seatNumber}
                </button>
              );
            })}
          </div>
        )}

        {selectedSeats.length > 0 && (
          <div className="mt-10 rounded-xl border border-slate-700 bg-slate-800 p-5">
            <div className="flex items-center justify-between">
              <span className="text-slate-300">
                {selectedSeats.length} selected
              </span>

              <span className="text-xl font-bold text-amber-400">
                {totalPrice.toLocaleString()} RSD
              </span>
            </div>

            <p className="mt-3 text-sm text-slate-400">
              Seats:{" "}
              {selectedSeats
                .map((seat) => `${seat.rowLabel}${seat.seatNumber}`)
                .join(", ")}
            </p>

            {bookingError && (
              <p className="mt-4 text-sm text-red-300">{bookingError}</p>
            )}

            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => void handleCreateBooking()}
              className="mt-6 w-full rounded-lg bg-amber-500 px-4 py-3 font-semibold text-slate-950 transition hover:bg-amber-400 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting ? "Creating booking..." : "Continue to payment"}
            </button>
          </div>
        )}

        <div className="mt-10 flex flex-wrap justify-center gap-5 text-sm text-slate-300">
          <span>🟩 Available</span>
          <span>🟨 Selected</span>
          <span>⬛ Unavailable</span>
        </div>
      </div>
    </section>
  );
}

export default ScreeningDetailsPage;
