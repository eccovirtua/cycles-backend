// src/pages/DashboardPage.tsx

import React, { useEffect, useState } from "react";

export default function DashboardPage() {
    const [users, setUsers] = useState<any[]>([]);
    const [error, setError] = useState("");

    const fetchUsers = async () => {
        try {
            const token = localStorage.getItem("token");
            if (!token) {
                setError("No hay token JWT disponible");
                return;
            }

            const res = await fetch("/users", {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                throw new Error(`Error ${res.status}: ${res.statusText}`);
            }

            const data = await res.json();
            setUsers(data);
        } catch (err: any) {
            setError(err.message || "Error al obtener usuarios");
        }
    };

    return (
        <div className="p-4">
            <header className="mb-6">
                <h1 className="text-3xl font-bold">Dashboard</h1>
            </header>

            <div className="mb-4">
                <button
                    onClick={fetchUsers}
                    className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
                >
                    Ver todos los usuarios
                </button>
            </div>

            {error && <p className="text-red-600">{error}</p>}

            <ul className="space-y-2">
                {users.map((user, index) => (
                    <li key={index} className="border p-3 rounded bg-gray-100">
                        <p><strong>Nombre:</strong> {user.name}</p>
                        <p><strong>Email:</strong> {user.email}</p>
                        <p><strong>Rol:</strong> {user.role}</p>
                    </li>
                ))}
            </ul>
        </div>
    );
}
