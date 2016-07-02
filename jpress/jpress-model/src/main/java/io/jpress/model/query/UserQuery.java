/**
 * Copyright (c) 2015-2016, Michael Yang 杨福海 (fuhai999@gmail.com).
 *
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.model.query;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.IDataLoader;

import io.jpress.model.User;
import io.jpress.template.Module;
import io.jpress.template.TemplateUtils;

public class UserQuery extends JBaseQuery {
	private static User MODEL = new User();

	public static List<User> findList(int page, int pagesize, String gender, String role, String status,
			String orderBy) {
		StringBuilder sqlBuilder = new StringBuilder("select * from user u ");
		LinkedList<Object> params = new LinkedList<Object>();

		boolean needWhere = true;
		needWhere = appendIfNotEmpty(sqlBuilder, "u.gender", gender, params, needWhere);
		needWhere = appendIfNotEmpty(sqlBuilder, "u.role", role, params, needWhere);
		needWhere = appendIfNotEmpty(sqlBuilder, "u.status", status, params, needWhere);

		buildOrderBy(orderBy, sqlBuilder);

		sqlBuilder.append(" LIMIT ?, ?");
		params.add(page - 1);
		params.add(pagesize);

		if (params.isEmpty()) {
			return MODEL.find(sqlBuilder.toString());
		} else {
			return MODEL.find(sqlBuilder.toString(), params.toArray());
		}

	}

	public static User findFirstFromMetadata(String key, Object value) {
		return MODEL.findFirstFromMetadata(key, value);
	}

	public static Page<User> paginate(int pageNumber, int pageSize) {
		return MODEL.doPaginate(pageNumber, pageSize);
	}

	public static long findCount() {
		return MODEL.doFindCount();
	}

	public static long findAdminCount() {
		return MODEL.doFindCount(" role = ? ", "administrator");
	}

	public static User findById(final BigInteger userId) {
		return MODEL.getCache(userId, new IDataLoader() {
			@Override
			public Object load() {
				return MODEL.findById(userId);
			}
		});
	}

	public static User findUserByEmail(final String email) {
		return MODEL.getCache(email, new IDataLoader() {
			@Override
			public Object load() {
				return MODEL.doFindFirst("email = ?", email);
			}
		});
	}

	public static User findUserByUsername(final String username) {
		return MODEL.getCache(username, new IDataLoader() {
			@Override
			public Object load() {
				return MODEL.doFindFirst("username = ?", username);
			}
		});
	}

	public static User findUserByPhone(final String phone) {
		return MODEL.getCache(phone, new IDataLoader() {
			@Override
			public Object load() {
				return MODEL.doFindFirst("phone = ?", phone);
			}
		});
	}

	public static boolean updateContentCount(User user) {
		long count = 0;
		List<Module> modules = TemplateUtils.currentTemplate().getModules();
		if (modules != null && !modules.isEmpty()) {
			for (Module m : modules) {
				long moduleCount = ContentQuery.findCountInNormalByModuleAndUserId(m.getName(), user.getId());
				count += moduleCount;
			}
		}

		user.setContentCount(count);
		return user.update();
	}

	public static boolean updateCommentCount(User user) {
		long count = CommentQuery.findCountByUserIdInNormal(user.getId());
		user.setCommentCount(count);
		return user.update();
	}

	private static void buildOrderBy(String orderBy, StringBuilder fromBuilder) {
		if ("content_count".equals(orderBy)) {
			fromBuilder.append(" ORDER BY u.content_count DESC");
		}

		else if ("comment_count".equals(orderBy)) {
			fromBuilder.append(" ORDER BY u.comment_count DESC");
		}

		else if ("username".equals(orderBy)) {
			fromBuilder.append(" ORDER BY u.username DESC");
		}

		else if ("nickname".equals(orderBy)) {
			fromBuilder.append(" ORDER BY u.nickname DESC");
		}

		else if ("amount".equals(orderBy)) {
			fromBuilder.append(" ORDER BY u.amount DESC");
		}

		else if ("logged".equals(orderBy)) {
			fromBuilder.append(" ORDER BY u.logged DESC");
		}

		else if ("activated".equals(orderBy)) {
			fromBuilder.append(" ORDER BY u.activated DESC");
		}

		else {
			fromBuilder.append(" ORDER BY u.created DESC");
		}
	}

}
