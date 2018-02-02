/*
 * Copyright (C) 2017 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.ale.api;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Riccardo De Benedictis
 */
public interface UserAPI {

    /**
     * Creates a new user given an e-mail, a password, a first name and a last
     * name.
     *
     * @param email a {@code String} representing the e-mail of the new user.
     * The e-mail field should by uniqe.
     * @param password a {@code String} representing the password of the new
     * user.
     * @param first_name a {@code String} representing the first name of the new
     * user.
     * @param last_name a {@code String} representing the last name of the new
     * user.
     * @return a {@link User} instance representing the newly created user.
     */
    public User new_user(String email, String password, String first_name, String last_name);

    public User get_user(long user_id);

    public Map<String, Parameter> get_parameter_types(long student_id);

    public Collection<User> find_users(String search_string);

    public User login(String email, String password);

    public void add_teacher(long student_id, long teacher_id);

    public void remove_teacher(long student_id, long teacher_id);

    public Collection<User> get_teachers(long student_id);

    public Collection<User> get_students(long teacher_id);
}
