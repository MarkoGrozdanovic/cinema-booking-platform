import { isAxiosError } from "axios";
import { useEffect, useState } from "react";
import { useFieldArray, useForm, type SubmitHandler } from "react-hook-form";
import { useNavigate } from "react-router";
import { getAllCinemas } from "../../api/cinemaApi";
import { createHall } from "../../api/hallApi";
import type { Cinema } from "../../types/cinema";
import type { CreateHallRequest, HallType, SeatType } from "../../types/hall";

const hallTypes: HallType[] = ["STANDARD", "THREE_D", "IMAX", "VIP"];

const seatTypes: SeatType[] = [
  "STANDARD",
  "VIP",
  "COUPLE",
  "WHEELCHAIR_ACCESSIBLE",
];

function CreateHallPage() {
  const navigate = useNavigate();

  const [cinemas, setCinemas] = useState<Cinema[]>([]);
  const [isLoadingCinemas, setIsLoadingCinemas] = useState(true);
  const [cinemaError, setCinemaError] = useState<string | null>(null);

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<CreateHallRequest>({
    defaultValues: {
      name: "",
      hallType: "STANDARD",
      rows: [
        {
          rowLabel: "A",
          numberOfSeats: 10,
          seatType: "STANDARD",
        },
      ],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: "rows",
  });

  useEffect(() => {
    let cancelled = false;

    async function loadCinemas() {
      try {
        const response = await getAllCinemas();

        if (!cancelled) {
          setCinemas(response.filter((cinema) => cinema.active));
        }
      } catch {
        if (!cancelled) {
          setCinemaError("Unable to load active cinemas.");
        }
      } finally {
        if (!cancelled) {
          setIsLoadingCinemas(false);
        }
      }
    }

    void loadCinemas();

    return () => {
      cancelled = true;
    };
  }, []);

  const onSubmit: SubmitHandler<CreateHallRequest> = async (request) => {
    try {
      await createHall(request);
      navigate("/admin/halls");
    } catch (error) {
      const message = isAxiosError<{ message?: string }>(error)
        ? error.response?.data.message
        : undefined;

      setError("root", {
        message: message ?? "Unable to create cinema hall.",
      });
    }
  };

  function addRow() {
    if (fields.length >= 26) {
      return;
    }

    const nextLabel = String.fromCharCode(65 + fields.length);

    append({
      rowLabel: nextLabel,
      numberOfSeats: 10,
      seatType: "STANDARD",
    });
  }

  const inputClassName =
    "mt-2 w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 outline-none transition focus:border-amber-400";

  return (
    <section className="mx-auto max-w-4xl">
      <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
        Administration
      </p>

      <h1 className="mt-2 text-3xl font-bold">Add cinema hall</h1>

      <p className="mt-3 text-slate-400">
        Create a hall and define its physical seat rows.
      </p>

      <form
        onSubmit={(event) => void handleSubmit(onSubmit)(event)}
        className="mt-8 space-y-6 rounded-xl border border-slate-700 bg-slate-800 p-6"
      >
        <div className="grid gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="cinemaId">Cinema</label>

            <select
              id="cinemaId"
              disabled={isLoadingCinemas}
              {...register("cinemaId", {
                required: "Cinema is required.",
                valueAsNumber: true,
                min: {
                  value: 1,
                  message: "Cinema is required.",
                },
              })}
              className={inputClassName}
            >
              <option value="">
                {isLoadingCinemas ? "Loading cinemas..." : "Select a cinema"}
              </option>

              {cinemas.map((cinema) => (
                <option key={cinema.id} value={cinema.id}>
                  {cinema.name} — {cinema.city}
                </option>
              ))}
            </select>

            {errors.cinemaId && (
              <p className="mt-2 text-sm text-red-300">
                {errors.cinemaId.message}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="name">Hall name</label>

            <input
              id="name"
              {...register("name", {
                required: "Hall name is required.",
                maxLength: {
                  value: 50,
                  message: "Hall name must not exceed 50 characters.",
                },
              })}
              className={inputClassName}
            />

            {errors.name && (
              <p className="mt-2 text-sm text-red-300">{errors.name.message}</p>
            )}
          </div>
        </div>

        <div>
          <label htmlFor="hallType">Hall type</label>

          <select
            id="hallType"
            {...register("hallType", {
              required: "Hall type is required.",
            })}
            className={inputClassName}
          >
            {hallTypes.map((hallType) => (
              <option key={hallType} value={hallType}>
                {hallType.replaceAll("_", " ")}
              </option>
            ))}
          </select>
        </div>

        <div>
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-xl font-semibold">Seat rows</h2>

              <p className="mt-1 text-sm text-slate-400">
                Configure the label, size, and seat type for each row.
              </p>
            </div>

            <button
              type="button"
              disabled={fields.length >= 26}
              onClick={addRow}
              className="rounded-lg border border-amber-500 px-4 py-2 font-semibold text-amber-300 transition hover:bg-amber-950/30 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Add row
            </button>
          </div>

          <div className="mt-5 space-y-4">
            {fields.map((field, index) => (
              <div
                key={field.id}
                className="grid gap-4 rounded-lg border border-slate-700 bg-slate-900/60 p-4 md:grid-cols-[1fr_1fr_2fr_auto]"
              >
                <div>
                  <label
                    htmlFor={`rows.${index}.rowLabel`}
                    className="text-sm text-slate-300"
                  >
                    Row label
                  </label>

                  <input
                    id={`rows.${index}.rowLabel`}
                    {...register(`rows.${index}.rowLabel`, {
                      required: "Row label is required.",
                      pattern: {
                        value: /^[A-Za-z]{1,2}$/,
                        message: "Use one or two letters.",
                      },
                    })}
                    className={inputClassName}
                  />

                  {errors.rows?.[index]?.rowLabel && (
                    <p className="mt-2 text-sm text-red-300">
                      {errors.rows[index]?.rowLabel?.message}
                    </p>
                  )}
                </div>

                <div>
                  <label
                    htmlFor={`rows.${index}.numberOfSeats`}
                    className="text-sm text-slate-300"
                  >
                    Seats
                  </label>

                  <input
                    id={`rows.${index}.numberOfSeats`}
                    type="number"
                    min="1"
                    max="50"
                    {...register(`rows.${index}.numberOfSeats`, {
                      required: "Seat count is required.",
                      valueAsNumber: true,
                      min: {
                        value: 1,
                        message: "Minimum is 1.",
                      },
                      max: {
                        value: 50,
                        message: "Maximum is 50.",
                      },
                    })}
                    className={inputClassName}
                  />

                  {errors.rows?.[index]?.numberOfSeats && (
                    <p className="mt-2 text-sm text-red-300">
                      {errors.rows[index]?.numberOfSeats?.message}
                    </p>
                  )}
                </div>

                <div>
                  <label
                    htmlFor={`rows.${index}.seatType`}
                    className="text-sm text-slate-300"
                  >
                    Seat type
                  </label>

                  <select
                    id={`rows.${index}.seatType`}
                    {...register(`rows.${index}.seatType`, {
                      required: "Seat type is required.",
                    })}
                    className={inputClassName}
                  >
                    {seatTypes.map((seatType) => (
                      <option key={seatType} value={seatType}>
                        {seatType.replaceAll("_", " ")}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  type="button"
                  disabled={fields.length === 1}
                  onClick={() => remove(index)}
                  className="self-end rounded-lg border border-red-500 px-4 py-3 font-semibold text-red-300 transition hover:bg-red-950/40 disabled:cursor-not-allowed disabled:opacity-40"
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        </div>

        {cinemaError && (
          <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
            {cinemaError}
          </div>
        )}

        {errors.root && (
          <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
            {errors.root.message}
          </div>
        )}

        <button
          type="submit"
          disabled={isSubmitting || isLoadingCinemas || cinemaError !== null}
          className="w-full rounded-lg bg-amber-500 px-4 py-3 font-semibold text-slate-950 transition hover:bg-amber-400 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting ? "Creating hall..." : "Create hall"}
        </button>
      </form>
    </section>
  );
}

export default CreateHallPage;
