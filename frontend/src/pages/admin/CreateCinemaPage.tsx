import { isAxiosError } from "axios";
import { useForm, type SubmitHandler } from "react-hook-form";
import { useNavigate } from "react-router";
import { createCinema } from "../../api/cinemaApi";
import type { CreateCinemaRequest } from "../../types/cinema";

function CreateCinemaPage() {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<CreateCinemaRequest>();

  const onSubmit: SubmitHandler<CreateCinemaRequest> = async (request) => {
    try {
      await createCinema(request);
      navigate("/admin/cinemas");
    } catch (error) {
      const message = isAxiosError<{ message?: string }>(error)
        ? error.response?.data.message
        : undefined;

      setError("root", {
        message: message ?? "Unable to create cinema.",
      });
    }
  };

  const inputClassName =
    "mt-2 w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 outline-none transition focus:border-amber-400";

  return (
    <section className="mx-auto max-w-2xl">
      <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
        Administration
      </p>

      <h1 className="mt-2 text-3xl font-bold">Add cinema</h1>

      <p className="mt-3 text-slate-400">
        Add a cinema location to the platform.
      </p>

      <form
        onSubmit={(event) => void handleSubmit(onSubmit)(event)}
        className="mt-8 space-y-5 rounded-xl border border-slate-700 bg-slate-800 p-6"
      >
        <div>
          <label htmlFor="name">Cinema name</label>

          <input
            id="name"
            {...register("name", {
              required: "Cinema name is required.",
              maxLength: {
                value: 100,
                message: "Cinema name must not exceed 100 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.name && (
            <p className="mt-2 text-sm text-red-300">{errors.name.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="address">Address</label>

          <input
            id="address"
            {...register("address", {
              required: "Address is required.",
              maxLength: {
                value: 200,
                message: "Address must not exceed 200 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.address && (
            <p className="mt-2 text-sm text-red-300">
              {errors.address.message}
            </p>
          )}
        </div>

        <div>
          <label htmlFor="city">City</label>

          <input
            id="city"
            {...register("city", {
              required: "City is required.",
              maxLength: {
                value: 100,
                message: "City must not exceed 100 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.city && (
            <p className="mt-2 text-sm text-red-300">{errors.city.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="description">Description</label>

          <textarea
            id="description"
            rows={4}
            {...register("description", {
              maxLength: {
                value: 1000,
                message: "Description must not exceed 1000 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.description && (
            <p className="mt-2 text-sm text-red-300">
              {errors.description.message}
            </p>
          )}
        </div>

        {errors.root && (
          <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
            {errors.root.message}
          </div>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-lg bg-amber-500 px-4 py-3 font-semibold text-slate-950 transition hover:bg-amber-400 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting ? "Creating cinema..." : "Create cinema"}
        </button>
      </form>
    </section>
  );
}

export default CreateCinemaPage;
