/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js}"
    ],

    theme: {
        extend: {
            colors: {
                factory: {
                    bg: "#05080D",
                    panel: "#0B111A",
                    sub: "#101826",
                    border: "#1E3A4F",
                    blue: "#38BDF8",
                    cyan: "#22D3EE",
                }
            }
        }
    },

    plugins: [],
}